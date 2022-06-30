import pymysql
import socket

#bubblesort
def bubbleSort(nlist):
    for num in range(len(nlist)-1,0,-1):
        for i in range(num):
            if nlist[i]>nlist[i+1]:
                temp = nlist[i]
                nlist[i] = nlist[i+1]
                nlist[i+1] = temp
                
#get score and names from server
client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
#connect to server
client.connect((socket.gethostname(), 1024))
#recieve and decode data from server
message=client.recv(1024)
message = message.decode("utf-8")
#split into name and score variables
data = message.split(" ")
name = data[0]
score = data[1]

#connect to mySQL database remotely
connection = pymysql.connect(
    host = 'remotemysql.com',
    port = 3306,
    user = 'AFvnG8ZbT5',
    passwd = 'l1TS11gYLz',
    db = 'AFvnG8ZbT5',
    charset = 'utf8mb4',
    cursorclass = pymysql.cursors.DictCursor
    )

myCursor = connection.cursor()

#querying all scores on the leaderboard
myCursor.execute("SELECT score FROM highscore")

#getting all values into a simple list without any dictionaries
s = myCursor.fetchall()
for i in range(10):
    s[i] = list(s[i].values())
    s[i] = s[i][0]
    
#sort data
bubbleSort(s)

#finding rank of new score
rank = -1
loop = 0
for i in range(len(s)):
    if int(score)>s[i]:
        rank += 1
        loop += 1
rank = 10 - rank

#updating the ranks of all other scores
for i in range(loop - 1, -1, -1):
    myCursor.execute("UPDATE highscore SET ranking='{}' WHERE ranking={};".format(rank+i+1, rank+i))

#adding new score
myCursor.execute("INSERT INTO highscore(name, score, ranking) VALUES('{}', '{}', '{}');".format(name, score, rank))

#deleting the lowest score
myCursor.execute("DELETE FROM highscore WHERE ranking=11;")

#update database
connection.commit()

#close connection
connection.close()

#confirm data sent and that its okay for the server to close
client.send(bytes("recieved", "utf-8"))

