from flask import Flask,request
import bcrypt
from flask_sqlalchemy import SQLAlchemy

login_register_backend =  Flask(__name__)

login_register_backend.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///login_register_backend.db'
login_register_backend.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(login_register_backend)

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(50), primary_key=False)
    password = db.Column(db.String(128), primary_key=False)

#Test connection
@login_register_backend.route('/test')
def test():
    return 'Login Backend Operational'

#Register users
@login_register_backend.route('/register', methods=['POST'])
def register():
    username = request.form['username']
    password = request.form['password']

    user_exists = User.query.filter_by(username=username).first()

    password_bytes = password.encode('utf-8')

    salt = bcrypt.gensalt()

    hashed_password = bcrypt.hashpw(password_bytes,salt)

    if user_exists:
        return "Username taken"
    
    else:
        new_user = User(username=username, password=hashed_password)

        db.session.add(new_user)
        db.session.commit()

        return "User Added Successfully!"


#Checks user details then logs them in
@login_register_backend.route('/login', methods=['POST'])
def login():
    username = request.form['username']
    password = request.form['password']

    existing_user = User.query.filter_by(username=username).first()

    password_bytes = password.encode('utf-8')

    check_password_bytes = existing_user.password


    if existing_user:
        if bcrypt.checkpw(password_bytes,check_password_bytes):
            return "Logged in successfully"
        else:
            return "Incorrect username and password combination"    
    else:
        return "User does not exist"
    

if __name__ == '__main__':
    with login_register_backend.app_context():
        db.create_all()
    login_register_backend.run(port=5000)