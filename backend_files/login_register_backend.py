from flask import Flask,request
import bcrypt
from flask_sqlalchemy import SQLAlchemy
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

#initialise application and database settings
login_register_backend =  Flask(__name__)

login_register_backend.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///login_register_backend.db'
login_register_backend.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(login_register_backend)

#used to stop excessive requests and prevent denial of servcice attacks 
limiter = Limiter(
    app=login_register_backend,
    key_func=get_remote_address,
    default_limits=["100 per hour","30 per minute"]

)

#user table is defined
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(50), primary_key=False)
    password = db.Column(db.String(128), primary_key=False)



'''
Used to test that the backend is operational

Returns a string to confirm server is operational
'''
@login_register_backend.route('/test')
def test():
    return 'Login Backend Operational'




'''
User is registered to the application

Parameters:
    username (str):chosen username
    password (string): chosen password


Returns:
    confirmation string
'''
@login_register_backend.route('/register', methods=['POST'])
def register():
    #details to be recieved from app
    username = request.form['username']
    password = request.form['password']

    #checks if username already exists before allowing registration
    user_exists = User.query.filter_by(username=username).first()

    #converted to bytes to be readied for encryption
    password_bytes = password.encode('utf-8')

    #salt used for extra security
    salt = bcrypt.gensalt(rounds=14)

    #password hashed before being stores
    hashed_password = bcrypt.hashpw(password_bytes,salt)


    #ensures username is valid
    if user_exists:
        return "Username taken"
    
    #ensures username is not empty
    elif (username==""): 
        return "Empty username"
    
    #if valid the user is added to the database and confirmation message is shown
    else:
        new_user = User(username=username, password=hashed_password)

        db.session.add(new_user)
        db.session.commit()

        return "User Added Successfully!"


'''
User is logged in to the application

Parameters:
    username (str):chosen username
    password (string): chosen password

Returns confirmation string
'''
@login_register_backend.route('/login', methods=['POST'])
def login():
    #details to be recieved from app
    username = request.form['username']
    password = request.form['password']

    #checks to see if user is present on the database
    existing_user = User.query.filter_by(username=username).first()

    #password converted to bytes to be checked against hash stored in database
    password_bytes = password.encode('utf-8')

    #hash in database
    check_password_bytes = existing_user.password

    #if user exists then check
    if existing_user:
        #is pasword is correct log users in
        if bcrypt.checkpw(password_bytes,check_password_bytes):
            return "Logged in successfully"
        #let user know username/password (password is the wrong one, but do not specify for security purpose)
        else:
            return "Incorrect username and password combination"        
    #Let user know username does not exist
    else:
        return "User does not exist"
    

#to run
if __name__ == '__main__':
    with login_register_backend.app_context():
        db.create_all()
    login_register_backend.run(host='0.0.0.0',port=5000)