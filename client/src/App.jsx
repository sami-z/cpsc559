import './App.css';
import Navbar from "./components/Navbar/Navbar";
import Files from "./components/Files/Files"
import Sidebar from "./components/Sidebar/Sidebar"
import { useState, useEffect } from 'react';
import { RESPONSE_QUEUE_SERVER_PORT } from './components/WebSocket/WebSocket';

function createWebSocket() {
  return new WebSocket(RESPONSE_QUEUE_SERVER_PORT);
}

function App() {
  
  const MINUTE_MS = 10000;
  const newWebSocket = createWebSocket();
  
  useEffect(() => {
    const interval = setInterval(() => {
      console.log('Logs every 2 secs');
      
      
      console.log('WebSocket connection established!');

      console.log(newWebSocket);
      newWebSocket.send("{\"userName\":\"manbir\"}");
      console.log(newWebSocket);

      // Send the file then close
      // handleFileUpload();
      // handleClose();
        
    }, MINUTE_MS);

    return () => clearInterval(interval); // This represents the unmount function, in which you need to clear your interval to prevent memory leaks.
  }, [])

  return (
    <div className="App">
      <div className='app_main'>
        <Navbar/>
        <Sidebar/>
        <Files/>
      </div>
    </div>
  );
}

export default App;
