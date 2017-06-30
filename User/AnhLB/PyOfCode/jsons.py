import json
data = {"spam": "foo", "parrot": 42}
in_json = json.dumps(data) # Encode the data
in_json
json.loads(in_json) # Decode into a Python object (dictionary)
