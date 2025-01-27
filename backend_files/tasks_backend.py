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
    hours_worked = db.Column(db.Integer, nullable=False)
    username = db.Column(db.String(25), nullable=False)


#Test connection
@tasks_backend.route("/test") 
def test(): 
    return "Task backend running"


#Functionality to add task
@tasks_backend.route("/add_task",methods=['POST'])
def add_task():
    title = request.form["title"]
    tag = request.form["tag"]
    hours_worked = request.form["hours_worked"]
    username = request.form["username"]
    
    
    new_task= Task(title=title,tag=tag,hours_worked=hours_worked,username=username)
    
    db.session.add(new_task)
    db.session.commit()
    
    return "Successfully added task"


if __name__ == '__main__':
    with tasks_backend.app_context():
        db.create_all()
    tasks_backend.run(port=5000)