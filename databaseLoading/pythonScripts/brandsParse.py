

# Open the workbook and define the worksheet
unparsedlines=[line.strip() for line in open('brands/restaurantBrands')]

#parse the lines to get the right fields (array of lines where each line has 14 fields)
finalLines=[]
for line in unparsedlines:
      temp=line.split("name\":")
      for segment in temp:
            tokens = segment.split("\"")
            if (tokens[1] != "total"):
                finalLines.append(tokens[1])


# Create a For loop to iterate through each line
file = open("finalBrands.txt", "w")

print len(finalLines)

finalLines=list(set(finalLines))
finalLines.sort()
for line in finalLines:
      file.write(line+"\n")

file.close()
print len(finalLines)
