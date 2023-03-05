import React, { useState } from 'react';
import { WEBSOCKET_URL } from '../WebSocket/WebSocket';

function ShareButton({ socket }) {
  const [name, setName] = useState('');

  const handleNameChange = (event) => {
    setName(event.target.value);
  };

function createWebSocket() {
    return new WebSocket(WEBSOCKET_URL);
}

  const handleSharePermission = () => {
    const newWebSocket = createWebSocket();
    // Send a JSON payload through the WebSocket connection to share the permission with the specified user
    const payload = { userName: "manbir", fileName: "samiSmart.jpg", shareWith: name };

    newWebSocket.addEventListener('open', () => {
        console.log('WebSocket connection established!');

        console.log(newWebSocket);

        if ( newWebSocket && newWebSocket.readyState === WebSocket.OPEN) {
            newWebSocket.send(JSON.stringify(payload));
            alert('Permission shared successfully!');
        }

        else{
            console.log("WEB SOCKET CONNECTION IS NOT OPEN!")
        }

        newWebSocket.close()
        console.log(newWebSocket);

        // Send the file then close
        // handleFileUpload();
        // handleClose();
    });

    console.log(JSON.stringify(payload))

  };

  return (
    <div>
      <input type="text" placeholder="Enter name" value={name} onChange={handleNameChange} />
      <button onClick={handleSharePermission}>Share Permission</button>
    </div>
  );
}

export default ShareButton;
