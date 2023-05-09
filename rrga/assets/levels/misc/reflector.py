'''
this script should be used to get an anti-diagonally flipped texture tileset
which is needed for rendering all possible conifgurations of a given tile.

'''

from PIL import Image
import sys
import os

if len(sys.argv) == 1:
    print("usage: python3 reflector.py <insert_file_path> <optional tile size>")
    print("example: python3 reflector.py pacman.png")
    print("default tile size: 128px by 128px")
    sys.exit()

filepath = sys.argv[1]

if not os.path.isfile(filepath):
    print("please provide a filepath to an image")
    sys.exit()

# Load the texture file and convert it to RGBA format
texture = Image.open(filepath).convert("RGBA")

# Define the square size of each subregion
size = 128
if len(sys.argv) > 2:
    try:
        size = int(sys.argv[2])
    except e:
        size = 0

if size <= 0:
    print("please provide a valid integer tile size")
    sys.exit()

tiles = (texture.width // size, texture.height // size)

# Flip each subregion anti-diagonally
flipped_regions = []
for y in range(tiles[1]):
    for x in range(tiles[0]):
        # Crop the subregion
        box = (x * size, y * size,
               (x + 1) * size, (y + 1) * size)
        subregion = texture.crop(box)

        # Flip the subregion anti-diagonally
        flipped_subregion = Image.new("RGBA", subregion.size)
        for i in range(subregion.width):
            for j in range(subregion.height):
                pixel = subregion.getpixel((i,j))
                flipped_subregion.putpixel((j,i), pixel)

        # Append the flipped subregion to the list
        flipped_regions.append(flipped_subregion)

# Create a new texture by stitching together the flipped subregions
flipped_texture = Image.new("RGBA", texture.size)
for i, region in enumerate(flipped_regions):
    x = i % tiles[0]
    y = i // tiles[0]
    box = (x * size, y * size)
    flipped_texture.paste(region, box)

# Save the flipped texture to a new PNG file
filename = os.path.splitext(os.path.basename(filepath))[0]
flipped_texture.save(os.path.dirname(filepath) + "/" + filename + "_flipped.png")
