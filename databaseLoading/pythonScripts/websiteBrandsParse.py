

# Open the workbook and define the worksheet
unparsedlines=[line.strip() for line in open('brands/brandsFromNutritionix')]

#parse the lines to get the right fields (array of lines where each line has 14 fields)
finalLines=[]
for line in unparsedlines:
    finalLines.append(line.strip())


# Create a For loop to iterate through each line
file = open("finalBrandsNutritionixandWiki.txt", "w")

print len(finalLines)

finalLines=list(set(finalLines))
finalLines.sort()
for line in finalLines:
      file.write(line+"\n")

file.close()
print len(finalLines)
