from transformers import pipeline
from flask import Flask,request
tags_classifier= Flask(__name__)

#Initialising the zero shot classification pipeline
#Model used has been trained on MNLI dataset
cf = pipeline("zero-shot-classification",model="facebook/bart-large-mnli")

#Potential labels
candidate_labels = ["Work", "Exercise", "Personal", "Shopping", "Uni Work","Gardening"]

#Test connection
@tags_classifier.route("/test")
def test():
    return "test works"

@tags_classifier.route("/classify",methods=['POST'])
def classify():
    #Gets text input
    todo_item = request.form["value"]
    
    
    #Conduct classification
    result = cf(todo_item, candidate_labels)
    label = result['labels'][0]
    
    #Returns result
    return label

#To run
if __name__ == '__main__':
    tags_classifier.run(host='0.0.0.0',port=5000)