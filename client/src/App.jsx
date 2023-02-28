import './App.css';
import Navbar from "./components/Navbar/Navbar";
import Files from "./components/Files/Files"
import Sidebar from "./components/sidebar"
import { useState, useEffect } from 'react';
import { RESPONSE_QUEUE_SERVER_PORT } from './components/WebSocket/WebSocket';

function createWebSocket() {
  return new WebSocket(RESPONSE_QUEUE_SERVER_PORT);
}

function App() {
  
  const MINUTE_MS = 2000;
  const newWebSocket = createWebSocket();
  
  useEffect(() => {
    const interval = setInterval(() => {
      console.log('Logs every 2 secs');
      
      
      console.log('WebSocket connection established!');

      console.log(newWebSocket);
      newWebSocket.send("{\"userName\":\"John\"}");
      console.log(newWebSocket);

      // Send the file then close
      // handleFileUpload();
      // handleClose();
        
    }, MINUTE_MS);

    return () => clearInterval(interval); // This represents the unmount function, in which you need to clear your interval to prevent memory leaks.
  }, [])

  return (
    <div className="App">
      <Navbar/>
      <Sidebar/>
      <Files/>
    </div>
  );
}

export default App;
