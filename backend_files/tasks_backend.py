from flask import Flask,request
from flask_sqlalchemy import SQLAlchemy

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
    
    
    new_task= Task(id=id,title=title,tag=tag,hours=hours,username=username)
    
    db.session.add(new_task)
    db.session.commit()
    
    return "Successfully added task"

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

if __name__ == '__main__':
    with tasks_backend.app_context():
        db.create_all()
    tasks_backend.run(port=4998)