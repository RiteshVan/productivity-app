from flask import Flask,request,render_template,jsonify
from flask_sqlalchemy import SQLAlchemy
from werkzeug.utils import secure_filename
import os

#Initialise app
image_diary_backend = Flask(__name__)
image_diary_backend.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///images.db'
image_diary_backend.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
image_diary_backend.config['UPLOAD_FOLDER'] = 'static/uploads'
image_diary_backend.config['UPLOAD_EXTENSIONS'] =[".jpeg"] #only accept jpegs for safety purposes

#database initialised
db =  SQLAlchemy(image_diary_backend)

#ensure folder to hold images is present
os.makedirs(image_diary_backend.config['UPLOAD_FOLDER'], exist_ok=True)

#Image model is defined
class Image(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    caption = db.Column(db.String(150), nullable=False)
    image = db.Column(db.String(150),nullable=False)
    username = db.Column(db.String(150),nullable=False)

'''
Used to check if backend is operational

Returns string to confirm to users/developers
'''
@image_diary_backend.route('/test')
def test():
    return 'Image Backend Operational'



'''
Used to upload image and caption to the backend

Parameters:
    image (file):Image to be added to backend
    caption (string):Caption for the image
    username (string): to associate each image to specific person

Return:
    confirmation message that string has been added
'''
@image_diary_backend.route('/upload', methods=['POST'])
def upload():
    #details to be obtained from application
    image = request.files['image']
    caption = request.form['caption']
    username = request.form['username']


    #if a suitable image has been provided, check to see if file name is safe
    #then add to folder and ensure path to it is stored on the database for access
    if image:
        image_name = secure_filename(image.filename)

        image.save(os.path.join(image_diary_backend.config['UPLOAD_FOLDER'],image_name))

        new_image = Image(image=image_name,caption=caption,username=username)
        db.session.add(new_image)
        db.session.commit()

    return "added image"




'''
Sends all images and caption to be processed by the app

Returns:
    dictionary of all image details
'''
@image_diary_backend.route('/get_images', methods=['GET'])
def get_images():
    #all images are obtained from the backend
    images = Image.query.all()

    #Images are placed into list of dictionaries with their details
    images_list = [{

        #URL link used by Glide library in Android Studio to get images in database
        'image':f"http://192.168.1.112:4997/static/uploads/{image.image}",
        'caption':image.caption
    } for image in images]
        
    #Sent to be viewed in app along with respective captions
    return {"images":images_list}


#same as earlier function but tasks are only obtained based on user logged in
@image_diary_backend.route('/get_images/<username>', methods=['GET'])
def get_images_by_user(username):
    images = Image.query.filter_by(username=username).all()

    images_list = [{
        
        'image':f"http://192.168.1.112:4997/static/uploads/{image.image}",
        'caption':image.caption
    } for image in images]
        
    return {"images":images_list}


#to run
if __name__ == '__main__':
    with image_diary_backend.app_context():
        db.create_all()
    image_diary_backend.run(host='0.0.0.0',port=4997)
