
#import xlrd
import MySQLdb
#import psycopg2

# Open the workbook and define the worksheet
unparsedlines=[line.strip() for line in open('FNDDS_2011_2012/FNDDSNutVal.txt')]

#parse the lines to get the right fields (array of lines where each line has 14 fields)
finalLines=[]
for line in unparsedlines:
      temp=line.split("^")
 #     lastcouple=temp[-1]
      temp=temp[0:len(temp)-1]
      finalLines.append(temp)
     # print temp



# Establish a MySQL connection
#database = psycopg2.connect(host="mysql.csail.mit.edu", user="slsNutrition", password="slsNutrition", dbname="nutritionData")
database = MySQLdb.connect(host="mysql.csail.mit.edu", user="slsNutrition", passwd="slsNutrition", db="FNDDS")

# Get the cursor, which is used to traverse the database, line by line
cursor = database.cursor()

# Create the INSERT INTO sql query
query = """INSERT INTO ModNutVal (food_id, nutrient_id, start, end,nut_value) VALUES (%s, %s, %s, %s, %s)"""

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


