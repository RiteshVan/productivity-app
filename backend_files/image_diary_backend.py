from flask import Flask,request,render_template,jsonify
from flask_sqlalchemy import SQLAlchemy
from werkzeug.utils import secure_filename
import os


image_diary_backend = Flask(__name__)
image_diary_backend.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///images.db'
image_diary_backend.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
image_diary_backend.config['UPLOAD_FOLDER'] = 'static/uploads'

db =  SQLAlchemy(image_diary_backend)

os.makedirs(image_diary_backend.config['UPLOAD_FOLDER'], exist_ok=True)

class Image(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    caption = db.Column(db.String(150), nullable=False)
    image = db.Column(db.String(150),nullable=False)


@image_diary_backend.route('/test')
def test():
    return 'Image Backend Operational'

@image_diary_backend.route('/upload', methods=['POST'])
def upload():
    image = request.files['image']
    caption = request.form['caption']

    if image:
        image_name = secure_filename(image.filename)
        image_path = os.path.join(image_diary_backend.config['UPLOAD_FOLDER'],image_name)

        image.save(image_path)

        new_image = Image(image=image_name,caption=caption)
        db.session.add(new_image)
        db.session.commit()

    return "added image"

@image_diary_backend.route('/get_images', methods=['GET'])
def get_images():

    images = Image.query.all()
    images_list = [{

        'image':f"http://192.168.1.112:4997/static/uploads/{image.image}",
        'caption':image.caption
    } for image in images]
        
    return {"images":images_list}


if __name__ == '__main__':
    with image_diary_backend.app_context():
        db.create_all()
    image_diary_backend.run(host='0.0.0.0',port=4997)