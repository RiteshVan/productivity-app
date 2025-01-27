from flask import Flask
from flask_sqlalchemy import SQLAlchemy

login_register_backend =  Flask(__name__)

login_register_backend.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///login_register_backend.db'
login_register_backend.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(login_register_backend)

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(50), primary_key=False)
    password = db.Column(db.String(50), primary_key=False)


#Test connection
@login_register_backend.route('/test')
def test():
    return 'Login Backend Operational'

if __name__ == '__main__':
    login_register_backend.run(port=5000)

