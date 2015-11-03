
#import xlrd
import MySQLdb
#import psycopg2

# Open the workbook and define the worksheet
unparsedlines=[line.strip() for line in open('FOOD_DES.txt')]

#parse the lines to get the right fields (array of lines where each line has 14 fields)
finalLines=[]
for line in unparsedlines:
      temp=line.split("~^~")
      lastcouple=temp[-1]
      temp=temp[0:len(temp)-1]
      temp+=lastcouple.split("^")
      temp[0] = temp[0][1:]
      finalLines.append(temp)
      #print temp

#print "final lines"
#print len(finalLines)

# Establish a MySQL connection
#database = psycopg2.connect(host="mysql.csail.mit.edu", user="slsNutrition", password="slsNutrition", dbname="nutritionData")
database = MySQLdb.connect(host="mysql.csail.mit.edu", user="slsNutrition", passwd="slsNutrition", db="nutritionData")

# Get the cursor, which is used to traverse the database, line by line
cursor = database.cursor()

# Create the INSERT INTO sql query
query = """INSERT INTO FOOD_DES_TEST (NDB_No, FdGrp_Cd, Long_Desc, Shrt_Desc, ComName, ManufacName, Survey, Ref_desc, Refuse, SciName, N_Factor, Pro_Factor, Fat_Factor, CHO_Factor) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""

# Create a For loop to iterate through each line
for line in finalLines:
      # Execute sql Query
      #print len(line)
      #print line
      cursor.execute(query, line)

print "all done"

# Close the cursor
cursor.close()

# Commit the transaction
database.commit()

# Close the database connection
database.close()


