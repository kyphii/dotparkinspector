import model.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class DotParkProcessor {
    public final static String FILE_TYPE_PARK = "park";
    public final static String FILE_TYPE_PARKOBJ = "parkobj";
    public final static String FILE_TYPE_DAT = "DAT";

    public final static int CHUNK_ID_OBJECTS = 2; //see ParkFileChunkType in ParkFile.cpp
    public final static int CHUNK_ID_PACKED_OBJECTS = 128; //0x80

    public final static int SCAN_RESULT_SUCCESS = 0;
    public final static int SCAN_RESULT_IO_EXCEPTION = 1;
    public final static int SCAN_RESULT_BAD_FILE = 2;

    public final static int BUFFER_SIZE = 1024;

    private final ByteBuffer buffer;

    //File position tracker
    private long globalFileOffset;

    //Might be needed later
    private int parkFileTargetVersion;

    public DotParkProcessor() {
        buffer = ByteBuffer.allocate(BUFFER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public int scanParkFile(final File parkFilePath) {
        FileInputStream dotParkStream;
        GZIPInputStream gzipStream;
        try {

            String path = parkFilePath.getPath();
            if (!FILE_TYPE_PARK.equals(Util.getFileExtension(path))) {
                return SCAN_RESULT_BAD_FILE;
            }
            Park park = new Park(Util.getFilePathWithoutName(parkFilePath), parkFilePath.getName());

            dotParkStream = new FileInputStream(parkFilePath);

            //Read the uncompressed header
            globalFileOffset = 0;
            Chunk[] chunkList = readParkHeader(dotParkStream);

            //Rest is compressed, can init the gzip input now that we're past the header
            globalFileOffset = 0;
            gzipStream = new GZIPInputStream(dotParkStream);

            //2nd chunk is unpacked objects (those packaged with base game + references to packed custom objs)
            if (skipToChunk(gzipStream, chunkList, CHUNK_ID_OBJECTS)) {
                readUnpackedObjectsChunk(gzipStream, park);
            }

            //TODO read packed object chunk
            if (skipToChunk(gzipStream, chunkList, CHUNK_ID_PACKED_OBJECTS)) {
                readPackedObjectsChunk(gzipStream, park);
            }

            gzipStream.close();
            dotParkStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return SCAN_RESULT_IO_EXCEPTION;
        }


        return SCAN_RESULT_SUCCESS;
    }

    private boolean skipToChunk(GZIPInputStream gzipStream, Chunk[] chunkList, int targetChunkId) throws IOException {
        int presentChunkIndex = getPresentChunkIndex(chunkList, targetChunkId);
        if (presentChunkIndex != -1) {
            skipBytes(gzipStream, chunkList[presentChunkIndex].getOffset() - globalFileOffset);
        }
        return (presentChunkIndex != -1);
    }

    private int getPresentChunkIndex(Chunk[] chunkList, int targetChunkId) {
        for (int i = 0; i < chunkList.length; ++i) {
            if (chunkList[i].getId() == targetChunkId) {
                return i;
            }
        }
        //Not present
        return -1;
    }

    private void readUnpackedObjectsChunk(InputStream is, Park park) throws IOException {
        //Number of sublists / ObjectTypes used
        short objectTypesCount = readShort(is);
        for (int i = 0; i < objectTypesCount; ++i) {
            int sublistCategoryId = readShort(is);
            ObjectCategory sublistObjectCategory = ObjectCategory.byId(sublistCategoryId);
            int sublistCount = readInt(is);
            for (int j = 0; j < sublistCount; ++j) {
                ObjectFileType oft = ObjectFileType.byId(readByte(is));

                switch (oft) {
                    case DESCRIPTOR_DAT:
                        ObjectEntry object = new ObjectEntry();
                        String objData = readFixedLength(is, 16);
                        object.setId(objData.substring(4, 12));
                        object.setObjectCategory(sublistObjectCategory);
                        park.getObjectGroupList()[sublistCategoryId].getEntries().add(object);
                        System.out.println(object);
                        break;
                    case DESCRIPTOR_JSON:
                        ParkObjectEntry parkObject = new ParkObjectEntry();
                        parkObject.setId(readString(is));
                        parkObject.setVersion(readString(is));
                        park.getObjectGroupList()[sublistCategoryId].getEntries().add(parkObject);
                        System.out.println(parkObject);
                        break;
                }
            }
            System.out.printf("Detected %d objects of type %s\n", sublistCount, sublistObjectCategory.toString());
        }
    }

    private void readPackedObjectsChunk(InputStream is, Park park) throws IOException {
        int objectCount = readInt(is);
        for (int i = 0; i < objectCount; ++i) {
            ObjectFileType oft = ObjectFileType.byId(readByte(is) + 1);
            String objectID;
            switch (oft) {
                case DESCRIPTOR_DAT:
                    System.out.println("Read a DAT object");
                    String objEntryData = readFixedLength(is, 16);
                    int objSize = readInt(is);
                    String objData = readFixedLength(is, objSize);

                    objectID = objEntryData.substring(4, 12);

                    ObjectEntry existingEntry = findObjectIdInPark(park, objectID);
                    //TODO Read the object data & assign it to an existing objectentry, or create a new one if none exists
                    break;
                case DESCRIPTOR_JSON:
                    System.out.println("Read a JSON/Parkobj object");
                    //TODO
                    break;
                default:
                    System.out.println("ERROR: Unsupported packed object detected");
                    break;
            }
        }
    }

    private ObjectEntry findObjectIdInPark(Park park, String objectId) {
        for (int i = 0; i < park.getObjectGroupList().length; ++i) {
            ObjectGroup group = park.getObjectGroupList()[i];
            ObjectEntry existingEntry = group.getEntries().stream().filter(
                    objectEntry -> objectEntry.getId().equals(objectId)).findFirst()
                    .orElse(null);
            if (existingEntry != null) {
                return existingEntry;
            }
        }
        return null;
    }

    private Chunk[] readParkHeader(FileInputStream dotParkStream) throws IOException {
        //.park header
        skipBytes(dotParkStream, 4);
        parkFileTargetVersion = readInt(dotParkStream);
        skipBytes(dotParkStream, 4);
        int numChunks = readInt(dotParkStream);
        skipBytes(dotParkStream, 48);

        //chunk table of contents
        Chunk[] chunkList = new Chunk[numChunks];
        for (int i = 0; i < numChunks; ++i) {
            chunkList[i] = new Chunk(
                    readInt(dotParkStream), //ID
                    readLong(dotParkStream), //Offset
                    readLong(dotParkStream)  //Length
            );
        }
        return chunkList;
    }

    private void skipBytes(InputStream is, long bytesToSkip) throws IOException {
        globalFileOffset += bytesToSkip;
        is.skipNBytes(bytesToSkip);
    }

    private byte readByte(InputStream is) throws IOException {
        buffer.clear();
        is.readNBytes(buffer.array(), 0, 4);
        globalFileOffset += 4; /// Bytes and Shorts are saved in 4-bytes in .park
        return buffer.get();
    }

    private short readShort(InputStream is) throws IOException {
        buffer.clear();
        is.readNBytes(buffer.array(), 0, 4); /// !!!
        globalFileOffset += 4; /// Shorts still take 4 bytes in .park format
        return buffer.getShort();
    }

    private int readInt(InputStream is) throws IOException {
        buffer.clear();
        is.readNBytes(buffer.array(), 0, 4);
        globalFileOffset += 4;
        return buffer.getInt();
    }

    private long readLong(InputStream is) throws IOException {
        buffer.clear();
        is.readNBytes(buffer.array(), 0, 8);
        globalFileOffset += 8;
        return buffer.getLong();
    }

    private String readFixedLength(InputStream is, final int length) throws IOException {
        int remainingLength = length;
        StringBuilder sb = new StringBuilder();
        while (remainingLength > BUFFER_SIZE) {
            buffer.clear();
            is.readNBytes(buffer.array(), 0, BUFFER_SIZE);
            globalFileOffset += BUFFER_SIZE;
            sb.append(StandardCharsets.UTF_8.decode(buffer));
            remainingLength -= BUFFER_SIZE;
        }
        buffer.clear();
        is.readNBytes(buffer.array(), 0, remainingLength);
        globalFileOffset += remainingLength;
        sb.append(StandardCharsets.UTF_8.decode(buffer).toString(), 0, remainingLength);

        return sb.toString();
    }

    private String readString(InputStream is) throws IOException {
        //Read a null-terminated string
        //there's probably a more optimal way to do this
        StringBuilder sb = new StringBuilder();
        char b = '1'; //throwaway value
        do {
            b = (char) is.read();
            sb.append(b);
            ++globalFileOffset;
        }
        while (b != '\0');
        return sb.toString();
    }
}
