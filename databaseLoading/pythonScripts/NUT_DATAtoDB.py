#import xlrd
import MySQLdb

# Open the workbook and define the worksheet
unparsedlines=[line.strip() for line in open('NUT_DATA.txt')]

#parse the lines to get the right fields (array of lines where each line has 18 fields)
finalLines=[]
for line in unparsedlines:
      temp=line.translate(None,'~').split("~^~")
      lastcouple=temp[-1]
      temp=temp[0:len(temp)-1]
      temp+=lastcouple.split("^")
      finalLines.append(temp)

#print "final lines"
#print len(finalLines)
#print finalLines[-1]
finalLines.pop(-1)
#print finalLines[-1]


# Establish a MySQL connection
database = MySQLdb.connect(host="mysql.csail.mit.edu", user="slsNutrition", passwd="slsNutrition", db="nutritionData")

# Get the cursor, which is used to traverse the database, line by line
cursor = database.cursor()

# Create the INSERT INTO sql query
query = """INSERT INTO NUT_DATA_TEST (NDB_No, Nutr_No, Nutr_Val, Num_Data_Pts, Std_Error, Src_Cd, Deriv_Cd, Ref_NDB_No, Add_Nutr_Mark, Num_Studies, Min, Max, DF, Low_EB, Up_EB, Stat_cmt, AddMod_Date, CC) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""

# Create a For loop to iterate through each line
for line in finalLines:
      # Execute sql Query
      #print len(line)
      print line
      cursor.execute(query, line)

print "all done"

# Close the cursor
cursor.close()

# Commit the transaction
database.commit()

# Close the database connection
database.close()
