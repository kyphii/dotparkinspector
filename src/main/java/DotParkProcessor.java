import model.Chunk;
import model.ObjectType;
import model.Park;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

    private final ByteBuffer numberBuffer;

    //File position tracker
    private long globalFileOffset;

    //Might be needed later
    private int parkFileTargetVersion;

    public DotParkProcessor() {
        numberBuffer = ByteBuffer.allocate(8);
        numberBuffer.order(ByteOrder.LITTLE_ENDIAN);
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

    private void readUnpackedObjectsChunk(GZIPInputStream compressedChunkStream, Park park) throws IOException {
        //Number of sublists / ObjectTypes used
        short objectTypesCount = readShort(compressedChunkStream);
        for (int i = 0; i < objectTypesCount; ++i) {
            short sublistTypeId = readShort(compressedChunkStream);
            ObjectType sublistObjectType = ObjectType.byId(sublistTypeId);
            int sublistCount = readInt(compressedChunkStream);
            if (sublistCount > 0) {
                //TODO
                System.out.printf("%s : %d%n", sublistObjectType, sublistCount);
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

    private short readShort(InputStream is) throws IOException {
        numberBuffer.clear();
        is.readNBytes(numberBuffer.array(), 0, 4); /// !!!
        globalFileOffset += 4; /// Shorts still take 4 bytes in .park format
        return numberBuffer.getShort();
    }

    private int readInt(InputStream is) throws IOException {
        numberBuffer.clear();
        is.readNBytes(numberBuffer.array(), 0, 4);
        globalFileOffset += 4;
        return numberBuffer.getInt();
    }

    private long readLong(InputStream is) throws IOException {
        numberBuffer.clear();
        is.readNBytes(numberBuffer.array(), 0, 8);
        globalFileOffset += 8;
        return numberBuffer.getLong();
    }
}
