from flask import Flask,request
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
from transformers import pipeline


tasks_backend = Flask(__name__)

tasks_backend.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///tasks_holder.db'
tasks_backend.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(tasks_backend)

#Uses zero shot classification pipeline
#Model trained on MNLI dataset
classifier = pipeline("zero-shot-classification" , model = "facebook/bart-large-mnli")


#Task model defined with required attributes
class Task(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(100), nullable=False)
    tag = db.Column(db.String(25), nullable=False)
    hours = db.Column(db.Integer, nullable=False)
    username = db.Column(db.String(25), nullable=False) 
    due_date = db.Column(db.Date, nullable=True)
  

#Same as task model but use to keep completed tasks on separate table
class CompletedTask(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(100), nullable=False)
    tag = db.Column(db.String(25), nullable=False)
    hours = db.Column(db.Integer, nullable=False)
    username = db.Column(db.String(25), nullable=False)
    due_date = db.Column(db.Date, nullable=True)


#Test connection
@tasks_backend.route("/test") 
def test(): 
    return "Task backend running"

#Functionality to add task
@tasks_backend.route("/add_task",methods=['POST'])
def add_task():
    #Details obtained from application
    id = request.form["id"]
    title = request.form["title"]
    tag = request.form["tag"]
    hours = request.form["hours"]
    username = request.form["username"]
    str_due_date = request.form.get("due_date")

    #Error handling in case user does not input the correct date
    due_date=None
    if str_due_date:
        try:
            due_date=datetime.strptime(str_due_date,'%Y-%m-%d')
        except ValueError:
            return 'error'
        
    #Database entry is created and added    
    new_task= Task(id=id,title=title,tag=tag,hours=hours,username=username,due_date=due_date)
    
    db.session.add(new_task)
    db.session.commit()
    
    #Success message shown to the user
    return "Successfully added task"


#Uses task ID to move to completed tasks table before deleting
@tasks_backend.route("/delete_task/<int:task_id>", methods=["DELETE"])
def delete_task(task_id):
    #Obtains the task from database
    task = db.session.get(Task, task_id)

    #If task is present, get details and add to completed table
    if task:

        completed_task = CompletedTask(
            id=task.id,
            title=task.title,
            tag=task.tag,
            hours=task.hours,
            username=task.username
        )

        db.session.add(completed_task)

        #Delete from database
        db.session.delete(task)
        db.session.commit()

        return {'message':f"Task {task_id} deleted"}
    else:
        return {"error":"Task not deleted"}
    

#Gets all tasks
@tasks_backend.route("/get_tasks")
def get_tasks():
    #All tasks obtained and turned into list of dictionaries
    tasks = Task.query.all()
    task_list = [{

        'id':task.id,
        'title':task.title,
        'tag':task.tag,
        'hours':task.hours,
        'username':task.username

    }
    for task in tasks
    ]

    #Tasks sent to app, where they are converted into visible tasks for the user
    return {"tasks":task_list}


#Same as prior function but only returns tasks specific to the user
@tasks_backend.route("/get_tasks/<username>")
def get_tasks_by_user(username):
    tasks = Task.query.filter_by(username=username).all()
    task_list = [{

        'id':task.id,
        'title':task.title,
        'tag':task.tag,
        'hours':task.hours,
        'username':task.username

    }
    for task in tasks
    ]

    return {"tasks":task_list}

#Similar to function from earlier, just gets tasks based on specific tags
@tasks_backend.route("/get_tasks_by_tag/<string:tag>") 
def get_tasks_by_tag(tag): 
    tasks = Task.query.filter_by(tag=tag).all()

    task_list = [{ 

        'id':task.id, 
        'title':task.title, 
        'tag':task.tag,
        'hours':task.hours, 
        'username':task.username 

        } 
        for task in tasks 
        ]
    
    return {"tasks":task_list}


#Only used to obtain completed tasks
#THIS FUNCTION IS NOT USED BY APPLICATION ONLY USED TO TEST WITH CURL
@tasks_backend.route("/get_completed_tasks")
def get_completed_tasks():
    completed_tasks = CompletedTask.query.all()

    completed_task_list = [{

        'id':task.id,
        'title':task.title,
        'tag':task.tag,
        'hours':task.hours,
        'username':task.username

    }
    for task in completed_tasks
    ]

    return {"completed_tasks":completed_task_list}

#Function is used to get hours worked per tag
@tasks_backend.route("/get_hours_per_tag")
def get_hours_per_tag():
    #All tasks are queried
    tasks = CompletedTask.query.all()
    
    #Dictionary to store hours per tag
    hours_per_tag = {}


    #Iterate through each task
    for task in tasks:
        #If task tag exists in dictionary, add hours to tag
        if task.tag in hours_per_tag:
            hours_per_tag[task.tag] += task.hours
        #if tag does not exist them, create then initialise the tag with the hours
        else:
            hours_per_tag[task.tag] = task.hours

    #Information sent to the application
    return {"hours_per_tag":hours_per_tag}


#Function used to sort by user, otherwise same functionality as prior
#Used to give data for analysis page
@tasks_backend.route("/get_hours_per_tag/<username>",methods=["GET"])
def get_hours_per_tag_by_user(username):
    tasks = CompletedTask.query.filter_by(username=username).all()
    
    hours_per_tag = {}

    for task in tasks:
        if task.tag in hours_per_tag:
            hours_per_tag[task.tag] += task.hours
        else:
            hours_per_tag[task.tag] = task.hours



    return {"hours_per_tag":hours_per_tag}



#Used for leaderboard feature
@tasks_backend.route("/get_hours_per_user")
def get_hours_per_user():
    #All finished tasks are queried
    tasks = CompletedTask.query.all()

    #Dictionary used to store hours worked per username
    hours_per_user = {}


    for task in tasks:
        if task.username in hours_per_user:
            #If username already present, add hours
            hours_per_user[task.username] += task.hours
        else:
            #If not present initialise
            hours_per_user[task.username] = task.hours


    #Send info to app
    return {"hours_per_user":hours_per_user}



#Function used to prioritise task with a tag
def prioritise_task(title,tag):
    #possible labels
    labels = ["urgent", "important", "routine task", "low priority"]
    
    #result from classification
    result = classifier(title,labels)

    #a score is assigned to each priority level
    #more urgency means more priority
    score_values={
        "urgent":1,
        "important":0.6,
        "routine task":0.3,
        "low priority":0.1
    }

    score = 0

    #Score weightages defined
    #Most probable label has the highest weight
    score+= result['scores'][0] * score_values[result['labels'][0]] * 1
    score+= result['scores'][1] * score_values[result['labels'][1]] * 0.6
    score+= result['scores'][2] * score_values[result['labels'][2]] * 0.3
    score+= result['scores'][3] * score_values[result['labels'][3]] * 0.1


    score = score/2 #To normalise the scores (divide by sum of weightages)


    #High importance tags mean higher score
    if tag == "Work" or tag == "Uni Work":
        score *= 2
    
    #More routine tags mean lower score
    if tag == "Gardening" or tag == "Personal":
        score = score/2
    
    #Rest of scores remain the same

    
    #Final score is returned
    return score


#Function used to give scores for each task
@tasks_backend.route("/get_prioritised_tasks/<username>")
def get_prioritised_tasks(username):
    #all tasks that are not yet completed are obtained
    tasks = Task.query.filter_by(username=username).all()

    #list of dictionaries is created and values are assigned
    prioritised_task_list = [{

        'id':task.id,
        'title':task.title,
        'tag':task.tag,
        'hours':task.hours,
        'username':task.username,
        'priority':prioritise_task(task.title,task.tag) * task.hours #More time required means higher priority score

    }
    for task in tasks
    ]

    #Tasks are sorted and returned in order of priority
    sorted_by_priority = sorted(prioritised_task_list, key=lambda d:d['priority'],reverse=True)

    print(sorted_by_priority)


    list = ""

    #The list is returned to the user as a string for simplicity and ease of understanding
    for task in prioritised_task_list:
        list+= task['title']+"\n"

    if list:
        return list
    else:
        return "No tasks scheduled"



#To run
if __name__ == '__main__':
    with tasks_backend.app_context():
        db.create_all()
    tasks_backend.run(host='0.0.0.0',port=4998)