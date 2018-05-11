package com.tivo.fe420.streamer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

@Component("streamView")
public class StreamView extends AbstractView {
	private static final Logger logger = LoggerFactory.getLogger(StreamView.class);

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		final File file = new File((String) model.get("movieName"));
		final RandomAccessFile movieFile = new RandomAccessFile(file, "r");

		long rangeStart = 0;
		long rangeEnd = 0;
		boolean isPart = false;
		try {
			long movieSize = movieFile.length();
			String range = request.getHeader("range");
			if (range != null) {
				if (range.endsWith("-")) {
					range = range + (movieSize - 1);
				}
				int idxm = range.trim().indexOf("-");// "-" 위치
				rangeStart = Long.parseLong(range.substring(6, idxm));
				rangeEnd = Long.parseLong(range.substring(idxm + 1));
				if (rangeStart > 0) {
					isPart = true;
				}
			} else {
				rangeStart = 0;
				rangeEnd = movieSize - 1;
			}
			long partSize = rangeEnd - rangeStart + 1;
			logger.debug("accepted range: {}", rangeStart + "-" + rangeEnd + "/" + partSize + " isPart:" + isPart);
			response.reset();
			response.setStatus(isPart ? 206 : 200);
			response.setContentType("video/mp4");

			response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + movieSize);
			response.setHeader("Accept-Ranges", "bytes");
			response.setHeader("Content-Length", "" + partSize);

			OutputStream out = response.getOutputStream();
			movieFile.seek(rangeStart);

			int bufferSize = 8 * 1024;
			byte[] buf = new byte[bufferSize];
			do {
				int block = partSize > bufferSize ? bufferSize : (int) partSize;
				int len = movieFile.read(buf, 0, block);
				out.write(buf, 0, len);
				partSize -= block;
			} while (partSize > 0);
		} catch (IOException e) {
			logger.debug("");
		} finally {
			movieFile.close();
		}
	}
}
