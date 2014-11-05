=== Quake Texture Tool ===

This tool will batch-convert any PNG files in the current working dir to the
Quake color palette and create a WAD2 file containing all converted textures.

Following files are supported:

 * "example.png": treated as a texture named "example" (the color map).

 * "example_norm.png": A normal map for lighting. If present, this tool will
   apply directional lighting and bake the result into the final output.

 * "example_glow.png" or "example_luma.png": Contains pixels that are supposed
   to glow in the dark. If present such pixels will be added to the colormap
   and be converted to the "fullbright" part of the Quake palette.


By default texture dimensions will be reduced by a factor of four, so a texture
with 512 x 512 pixels will result in a 128 x 128 output. This can be changed
via commandline arguments.

A complete list of arguments is available via the "-h" option.
