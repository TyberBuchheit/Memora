import socket
from threading import Thread
from Modules.handle_input import handle_context, handle_prompt
import json


client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)


def start_reciever ():
    Thread(target=receive).start()

def send (message):

    client.send(message.encode())
    print("Message sent: ", message)


def receive ():
    while True:
        message = client.recv(1024).decode("utf-8")
        print ("rec: ",message)
        data = json.loads(message)
        if data.get("type") == "context":
            handle_context(data)
        elif data.get("type") == "prompt":
            response = handle_prompt(data)
            send(json.dumps({"type": "response", "response": response}))


if(__name__ == "__main__"):
    print("Connecting to server...")
    client.connect(('localhost', 8087))
    send("{\"clientType\":0,\"id\":\"Server\"}")
    print("Connected to server.")
    start_reciever()


    while True:
        message = input()
        send(message)

from Modules.groq_client import get_response

from Modules.groq_client import get_response
