package baseServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class App extends NanoHTTPD {

    public App() throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }

    public static void main(String[] args) {
        try {        	
        	try {
    			Runtime.getRuntime().exec(new String[] {"E:/MPC-HC.1.7.9.x64/mpc-hc64.exe", "http://192.168.43.64:8080/"});
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            new App();
        } catch (IOException ioe) {
            System.err.println("Couldn't start streaming server:\n" + ioe);
        }
    }

  //Announce that the file server accepts partial content requests
//    private Response createResponse(Response.Status status, String mimeType,
//            InputStream message) {
//        Response res = newChunkedResponse(status, mimeType, message);
//        res.addHeader("Accept-Ranges", "bytes");
//        return res;
//    }

    @Override
    public Response serve(IHTTPSession session) {
       // String msg = "<html><body><h1>Hello server</h1>\n";
        //Map<String, String> parms = session.getParms();
        System.out.println(session.getQueryParameterString());
//        Map<String, List<String>> parms =
//                decodeParameters(session.getQueryParameterString());

//        if (parms.get("username") == null) {
//            msg += "<form action='?' method='get'>\n  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
//        } else {
//            msg += "<p>Hello, " + parms.get("username") + "!</p>";
//        }
//        return newFixedLengthResponse(msg + "</body></html>\n");
       
        
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream("e:/movies/Yeh Jawaani Hai Deewani 2013 Hindi DvDRip 720p (MP4) x264...Hon3y.mp4");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
  //      return new NanoHTTPD.Response(Status.OK, "audio/mpeg", fis);
        File f= new File("e:/movies/Yeh Jawaani Hai Deewani 2013 Hindi DvDRip 720p (MP4) x264...Hon3y.mp4");
        
        try {
			return serveFile(session.getUri(), session.getHeaders(),f, Files.probeContentType(f.toPath()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        catch(Exception e){
        	
        }
        return getInternalErrorResponse("Unable to get the type");
       // return newChunkedResponse(Status.OK, "video/mp4", fis);
    }
    
//    private Response serveFile(String uri, Map<String, String> header,
//            File file, String mime) {
//        Response res;
//        try {
//            // Calculate etag
//            String etag = Integer.toHexString((file.getAbsolutePath()
//                    + file.lastModified() + "" + file.length()).hashCode());
//
//            // Support (simple) skipping:
//            long startFrom = 0;
//            long endAt = -1;
//            String range = header.get("range");
//            if (range != null) {
//                if (range.startsWith("bytes=")) {
//                    range = range.substring("bytes=".length());
//                    int minus = range.indexOf('-');
//                    try {
//                        if (minus > 0) {
//                            startFrom = Long.parseLong(range
//                                    .substring(0, minus));
//                            endAt = Long.parseLong(range.substring(minus + 1));
//                        }
//                    } catch (NumberFormatException ignored) {
//                    }
//                }
//            }
//
//            // Change return code and add Content-Range header when skipping is
//            // requested
//            long fileLen = file.length();
//            if (range != null && startFrom >= 0) {
//                if (startFrom >= fileLen) {
//                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE,
//                            NanoHTTPD.MIME_PLAINTEXT, null);
//                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
//                    res.addHeader("ETag", etag);
//                } else {
//                    if (endAt < 0) {
//                        endAt = fileLen - 1;
//                    }
//                    long newLen = endAt - startFrom + 1;
//                    if (newLen < 0) {
//                        newLen = 0;
//                    }
//
//                    final long dataLen = newLen;
//                    FileInputStream fis = new FileInputStream(file) {
//                        @Override
//                        public int available() throws IOException {
//                            return (int) dataLen;
//                        }
//                    };
//                    fis.skip(startFrom);
//
//                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime,
//                            fis);
//                    res.addHeader("Content-Length", "" + dataLen);
//                    res.addHeader("Content-Range", "bytes " + startFrom + "-"
//                            + endAt + "/" + fileLen);
//                    res.addHeader("ETag", etag);
//                }
//            } else {
//                if (etag.equals(header.get("if-none-match")))
//                    res = createResponse(Response.Status.NOT_MODIFIED, mime, null);
//                else {
//                    res = createResponse(Response.Status.OK, mime,
//                            new FileInputStream(file));
//                    res.addHeader("Content-Length", "" + fileLen);
//                    res.addHeader("ETag", etag);
//                }
//            }
//        } catch (IOException ioe) {
//            res = createResponse(Response.Status.FORBIDDEN,
//                    NanoHTTPD.MIME_PLAINTEXT, null);
//        }
//
//        return res;
//    }
    
    Response serveFile(String uri, Map<String, String> header, File file, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            String ifRange = header.get("if-range");
            boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

            String ifNoneMatch = header.get("if-none-match");
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));

            // Change return code and add Content-Range header when skipping is
            // requested
            long fileLen = file.length();

            if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    FileInputStream fis = new FileInputStream(file);
                    fis.skip(startFrom);

                    res = NanoHTTPD.newFixedLengthResponse(Status.PARTIAL_CONTENT, mime, fis, newLen);
                    res.addHeader("Accept-Ranges", "bytes");
                    res.addHeader("Content-Length", "" + newLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {

                if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                    res = newFixedLengthResponse(Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes */" + fileLen);
                    res.addHeader("ETag", etag);
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified

                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                    res.addHeader("ETag", etag);
                } else {
                    // supply the file
                    res = newFixedFileResponse(file, mime);
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = getForbiddenResponse("Reading file failed.");
        }

        return res;
    }

    private Response newFixedFileResponse(File file, String mime) throws FileNotFoundException {
        Response res;
        res = NanoHTTPD.newFixedLengthResponse(Status.OK, mime, new FileInputStream(file), (int) file.length());
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }
    
    protected Response getForbiddenResponse(String s) {
        return NanoHTTPD.newFixedLengthResponse(Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: " + s);
    }

    protected Response getInternalErrorResponse(String s) {
        return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "INTERNAL ERROR: " + s);
    }

    protected Response getNotFoundResponse() {
        return NanoHTTPD.newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
    }    
    
    public static Response newFixedLengthResponse(IStatus status, String mimeType, String message) {
        Response response = newFixedLengthResponse(status, mimeType, message);
        response.addHeader("Accept-Ranges", "bytes");
        return response;
    }
}
