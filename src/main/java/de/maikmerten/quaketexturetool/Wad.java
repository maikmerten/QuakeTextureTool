package de.maikmerten.quaketexturetool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author maik
 */
public class Wad {
	
	private List<WadEntry> entries = new ArrayList<>();
	private WadHeader wadHeader = new WadHeader();
	
	
	private class WadHeader extends StreamOutput {
		private byte[] magic = "WAD2".getBytes(Charset.forName("US-ASCII"));
		private int numentries = -1;
		private int diroffset = -1;

		private int getSize() {
			return 12;
		}
		
		private void write(OutputStream os) throws Exception {
			if(numentries < 0) {
				throw new Exception("WadHeader numentries not set");
			}
			
			if(diroffset < 0) {
				throw new Exception("WadHeader diroffset not set");
			}
			
			os.write(magic);
			writeLittle(numentries, os);
			writeLittle(diroffset, os);
		}
	}
	
	private class WadDirEntry extends StreamOutput {
		int offset = -1; // position of entry in WAD
		int dsize; // size of entry in WAD
		int size; // size in memory
		byte type;
		byte compression;
		byte dummy1;
		byte dummy2;
		byte[] name = new byte[16];
		
		private void write(OutputStream os) throws Exception {
			if(offset < 0) {
				throw new Exception("offset of WAD entry not yet computed");
			}
			writeLittle(offset, os);
			writeLittle(dsize, os);
			writeLittle(size, os);
			os.write(type);
			os.write(compression);
			os.write(dummy1);
			os.write(dummy2);
			os.write(name);
		}
		
	}
	
	private class WadEntry {
		private WadDirEntry dirEntry;
		private byte[] data;
		
		private void write(OutputStream os) throws Exception {
			dirEntry.write(os);
			os.write(data);
		}
	}


	public void addMipTexture(String name, byte[] mipData) throws Exception {
		WadDirEntry wentry = new WadDirEntry();
		wentry.type = 0x44;
		wentry.dsize = mipData.length;
		wentry.size = mipData.length;
		wentry.compression = 0;

		byte[] namebytes = name.getBytes(Charset.forName("US-ASCII"));
		for(int i = 0; i < Math.min(15, namebytes.length); ++i) {
			wentry.name[i] = namebytes[i];
		}
	
		WadEntry wadEntry = new WadEntry();
		wadEntry.dirEntry = wentry;
		wadEntry.data = mipData;
		entries.add(wadEntry);
	}
	
	
	private void computeOffsets() {
		int offset = wadHeader.getSize();
		for(WadEntry wadEntry : entries) {
			wadEntry.dirEntry.offset = offset;
			offset += wadEntry.data.length;
		}
		
		wadHeader.diroffset = offset;
		wadHeader.numentries = entries.size();
	}
	
	
	public void write(OutputStream os) throws Exception {
		computeOffsets();
		
		// write initial WAD header
		wadHeader.write(os);
		
		// write data for all entries
		for(WadEntry entry : entries) {
			os.write(entry.data);
		}
		
		// finish with WAD directory
		for(WadEntry entry : entries) {
			entry.dirEntry.write(os);
		}
		
	}

	
}
