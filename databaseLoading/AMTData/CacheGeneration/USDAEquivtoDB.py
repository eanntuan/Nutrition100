
#import xlrd
import MySQLdb
#import psycopg2

# Open the workbook and define the worksheet
unparsedlines=[line.strip() for line in open('4_23/usdaToAdd.csv')]
#print unparsedlines

#parse the lines to get the right fields (array of lines where each line has 14 fields)
finalLines=[]
tokens=unparsedlines[0].split('\r')
for line in tokens:
      temp=line.split(",")
      temp[0] = temp[0].lower()
      finalLines.append(temp)
      #print temp
      #break



# Establish a MySQL connection
#database = psycopg2.connect(host="mysql.csail.mit.edu", user="slsNutrition", password="slsNutrition", dbname="nutritionData")
database = MySQLdb.connect(host="mysql.csail.mit.edu", user="slsNutrition", passwd="slsNutrition", db="nutritionData")

# Get the cursor, which is used to traverse the database, line by line
cursor = database.cursor()

# Create the INSERT INTO sql query
query = """INSERT INTO freebaseEquiv (name, fbid, srid) VALUES (%s, %s, %s)"""

# Create a For loop to iterate through each line
for line in finalLines:
      # Execute sql Query
      #print len(line)
      cursor.execute(query, line)

# Close the cursor
cursor.close()

# Commit the transaction
database.commit()

# Close the database connection
database.close()


