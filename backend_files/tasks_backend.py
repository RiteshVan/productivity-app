from flask import Flask,request
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

tasks_backend = Flask(__name__)

tasks_backend.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///tasks_holder.db'
tasks_backend.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(tasks_backend)

class Task(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(100), nullable=False)
    tag = db.Column(db.String(25), nullable=False)
    hours = db.Column(db.Integer, nullable=False)
    username = db.Column(db.String(25), nullable=False)
    due_date = db.Column(db.Date, nullable=True)
  
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
    id = request.form["id"]
    title = request.form["title"]
    tag = request.form["tag"]
    hours = request.form["hours"]
    username = request.form["username"]
    str_due_date = request.form.get("due_date")

    due_date=None
    if str_due_date:
        try:
            due_date=datetime.strptime(str_due_date,'%Y-%m-%d')
        except ValueError:
            return 'error'
    
    new_task= Task(id=id,title=title,tag=tag,hours=hours,username=username,due_date=due_date)
    
    db.session.add(new_task)
    db.session.commit()
    
    return "Successfully added task"


@tasks_backend.route("/delete_task/<int:task_id>", methods=["DELETE"])
def delete_task(task_id):
    task = db.session.get(Task, task_id)


    if task:

        completed_task = CompletedTask(
            id=task.id,
            title=task.title,
            tag=task.tag,
            hours=task.hours,
            username=task.username
        )

        db.session.add(completed_task)

        db.session.delete(task)
        db.session.commit()

        return {'message':f"Task {task_id} deleted"}
    else:
        return {"error":"Task not deleted"}
    

@tasks_backend.route("/get_tasks")
def get_tasks():
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

    return {"tasks":task_list}


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


@tasks_backend.route("/get_hours_per_tag")
def get_hours_per_tag():
    tasks = CompletedTask.query.all()
    
    hours_per_tag = {}

    for task in tasks:
        if task.tag in hours_per_tag:
            hours_per_tag[task.tag] += task.hours
        else:
            hours_per_tag[task.tag] = task.hours


    return {"hours_per_tag":hours_per_tag}


@tasks_backend.route("/get_hours_per_user")
def get_hours_per_user():
    tasks = CompletedTask.query.all()

    hours_per_user = {}

    for task in tasks:
        if task.username in hours_per_user:
            hours_per_user[task.username] += task.hours
        else:
            hours_per_user[task.username] = task.hours

    return {"hours_per_user":hours_per_user}



if __name__ == '__main__':
    with tasks_backend.app_context():
        db.create_all()
    tasks_backend.run(host='0.0.0.0',port=4998)