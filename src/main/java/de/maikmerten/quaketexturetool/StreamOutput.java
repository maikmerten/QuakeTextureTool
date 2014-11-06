package de.maikmerten.quaketexturetool;

import java.io.OutputStream;

/**
 *
 * @author maik
 */
public abstract class StreamOutput {
		public void writeLittle(int i, OutputStream os) throws Exception {
			os.write(i);
			os.write(i >>> 8);
			os.write(i >>> 16);
			os.write(i >>> 24);
		}	
}
