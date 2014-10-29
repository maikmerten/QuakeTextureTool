package de.maikmerten.quaketexturetool;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 *
 * @author maik
 */
public class Wad {
	
	private class WadHeader {
		private byte[] magic = "WAD2".getBytes(Charset.forName("US-ASCII"));
		private int numentries;
		private int diroffset;

		private int getSize() {
			return 12;
		}
		
		private void write(OutputStream os) throws Exception {
			os.write(magic);
			Wad.writeLittle(numentries, os);
			Wad.writeLittle(diroffset, os);
		}
	}
	
	private class WadEntry {
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
	
	
	public void addMipTexture(String name, List<byte[][]> mipData) {
		
		MipTexHeader mipHead = new MipTexHeader();
		byte[] namebytes = name.getBytes(Charset.forName("US-ASCII"));
		for(int i = 0; i < Math.min(15, namebytes.length); ++i) {
			mipHead.name[i] = namebytes[i];
		}
		mipHead.width = mipData.get(0).length;
		mipHead.height = mipData.get(0)[0].length;
		
		mipHead.mipOffsets = new int[mipData.size()];
		int offset = mipHead.getSize();
		for(int i = 0; i < mipData.size(); ++i) {
			mipHead.mipOffsets[i] = offset;
			byte[][] mip = mipData.get(i);
			offset += mip.length * mip[0].length;
		}
		
		WadEntry wentry = new WadEntry();
		wentry.type = 0x44;
		wentry.dsize = offset;
		wentry.size = offset;
		wentry.compression = 0;
		wentry.name = mipHead.name;
		
	}
	
	
}
