import model.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
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

    private void readUnpackedObjectsChunk(GZIPInputStream gzipStream, Park park) throws IOException {
        //Number of sublists / ObjectTypes used
        short objectTypesCount = readShort(gzipStream);
        for (int i = 0; i < objectTypesCount; ++i) {
            ObjectCategory sublistObjectCategory = ObjectCategory.byId(readShort(gzipStream));
            int sublistCount = readInt(gzipStream);
            for (int j = 0; j < sublistCount; ++j) {
                ObjectFileType oft = ObjectFileType.byId(readByte(gzipStream));

                switch (oft) {
                    case DESCRIPTOR_NONE:
                        break;
                    case DESCRIPTOR_DAT:
                        //TODO
                        ObjectEntry object = new ObjectEntry();
                        String objData = readFixedLength(gzipStream, 16);

                        object.setId(objData.substring(4, 12));
                        object.setObjectCategory(sublistObjectCategory);

                        System.out.println(object);
                        break;
                    case DESCRIPTOR_JSON:
                        ParkObjectEntry parkObject = new ParkObjectEntry();
                        parkObject.setId(readString(gzipStream));
                        parkObject.setVersion(readString(gzipStream));
                        System.out.println(parkObject);
                        break;

                }
            }
        }
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

    private String readFixedLength(InputStream is, int length) throws IOException {
        int maxLength = Math.min(length, BUFFER_SIZE);
        buffer.clear();
        is.readNBytes(buffer.array(), 0, maxLength);
        globalFileOffset += maxLength;

        return StandardCharsets.UTF_8.decode(buffer).toString().substring(0, maxLength);
    }

    private String readString(InputStream is) throws IOException {
        //Read a null-terminated string
        //there's probably a more optimal way to do this
        StringBuilder sb = new StringBuilder();
        char b = '1'; //throwaway value
        do {
            b = (char) is.read();
            sb.append(b);
        }
        while (b != '\0');
        return sb.toString();
    }
}
