package p2pApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import p2pApp.p2pIndexer.TableHandler;
import utility.MimeTypes;

public class StreamServer extends NanoHTTPD {

	public StreamServer() throws IOException {
		super(utility.Utilities.streamPort);
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		System.out.println("\nStream Running @ port: "+utility.Utilities.streamPort);
	}

	public void stopServer(){
		stop();
	}

	@Override
	public Response serve(IHTTPSession session) {

		System.out.println(session.getQueryParameterString());
		Map<String, List<String>> parms =
				decodeParameters(session.getQueryParameterString());

		if(parms.get("f")==null){
			return getInternalErrorResponse("Empty file requested.");
		}
		String path= TableHandler.getFilePath(parms.get("f").get(0));

		if(path==null || path.length()<5){
			return getInternalErrorResponse("Unable to locate the file.");
		}
		File f= new File(path);
		
		if(getFileType(f.getName())==0){
			return getInternalErrorResponse("File streaming not supported.");
		}
		
		try {
			String mimeType = MimeTypes.getInstance().getMimeType(f.getName());
			if (mimeType == null) {
				mimeType = Files.probeContentType(f.toPath());
			}
			return serveFile(session.getUri(), session.getHeaders(),f, mimeType);
		} catch (IOException e) {
		} 
		catch(Exception e){
		}
		return getInternalErrorResponse("Error while getting the file type.");
	}

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
		} catch (Exception e) {
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
		Response response = NanoHTTPD.newFixedLengthResponse(status, mimeType, message);
		response.addHeader("Accept-Ranges", "bytes");
		return response;
	}

	public static int getFileType(String filename){
		try{
			String type= MimeTypes.getInstance().getMimeType(filename);
			if(type.contains("audio"))
				return 1;
			if(type.contains("video"))
				return 2;
		}
		catch(Exception e){
			
		}
		return 0;
	}
}
