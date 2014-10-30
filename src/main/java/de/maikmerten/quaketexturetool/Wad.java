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
	
	
	private class WadHeader {
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
			Wad.writeLittle(numentries, os);
			Wad.writeLittle(diroffset, os);
		}
	}
	
	private class WadDirEntry {
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
			Wad.writeLittle(offset, os);
			Wad.writeLittle(dsize, os);
			Wad.writeLittle(size, os);
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
	
	
	private class MipTexHeader {
		private byte[] name = new byte[16]; // zero terminated
		private int width; // 4 bytes, little endian
		private int height; // 4 bytes, little endian
		private int[] mipOffsets = new int[4]; // four MIPs, first offset: 40 (header length)
		
		private int getSize() {
			return name.length + (2*4) + (mipOffsets.length * 4);
		}
		
		private void write(OutputStream os) throws Exception {
			os.write(name);
			Wad.writeLittle(width, os);
			Wad.writeLittle(height, os);
			for(int i = 0; i < mipOffsets.length; ++i) {
				Wad.writeLittle(mipOffsets[i], os);
			}
		}
		
	}
	
	private static void writeLittle(int i, OutputStream os) throws Exception {
		os.write(i);
		os.write(i >>> 8);
		os.write(i >>> 16);
		os.write(i >>> 24);
	}
	
	
	public void addMipTexture(String name, List<byte[][]> mipData) throws Exception {
		
		MipTexHeader mipHead = new MipTexHeader();
		byte[] namebytes = name.getBytes(Charset.forName("US-ASCII"));
		for(int i = 0; i < Math.min(15, namebytes.length); ++i) {
			mipHead.name[i] = namebytes[i];
		}
		mipHead.height = mipData.get(0).length;
		mipHead.width = mipData.get(0)[0].length;
		
		mipHead.mipOffsets = new int[mipData.size()];
		int offset = mipHead.getSize();
		for(int i = 0; i < mipData.size(); ++i) {
			mipHead.mipOffsets[i] = offset;
			byte[][] mip = mipData.get(i);
			offset += mip.length * mip[0].length;
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mipHead.write(baos);
		for(int i = 0; i < mipData.size(); ++i) {
			byte[][] mip = mipData.get(i);
			for(int x = 0; x < mip.length; ++x) {
				baos.write(mip[x]);
			}
		}
		
		byte[] entryData = baos.toByteArray();
		
		WadDirEntry wentry = new WadDirEntry();
		wentry.type = 0x44;
		wentry.dsize = entryData.length;
		wentry.size = entryData.length;
		wentry.compression = 0;
		wentry.name = mipHead.name;
		
		
		WadEntry wadEntry = new WadEntry();
		wadEntry.dirEntry = wentry;
		wadEntry.data = entryData;
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
	
	
	public static void main(String[] args) throws Exception {
		
		byte[][] mip0 = new byte[16][16];
		byte[][] mip1 = new byte[8][8];
		byte[][] mip2 = new byte[4][4];
		byte[][] mip3 = new byte[2][2];
		
		
		byte[] row = mip0[0];
		for(int i = 0; i < row.length; ++i) {
			row[i] = (byte)0xFF;
		}
		
		row = mip0[1];
		for(int i = 0; i < row.length; ++i) {
			row[i] = (byte)254;
		}
		
		
		List<byte[][]> mips = new ArrayList<>(4);
		mips.add(mip0);
		mips.add(mip1);
		mips.add(mip2);
		mips.add(mip3);
		
		Wad wad = new Wad();
		wad.addMipTexture("abcde", mips);
		
		FileOutputStream fos = new FileOutputStream(new File("/tmp/test.wad"));
		wad.write(fos);
		
	}
	
	
}
