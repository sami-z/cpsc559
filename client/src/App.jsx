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
  
  const MINUTE_MS = 500;
  const [files, setFiles] = useState([]);
  
  useEffect(() => {
    const interval = setInterval(() => {
      console.log('Logs every 2 secs');
      const newWebSocket = createWebSocket();

      newWebSocket.onopen = () => {
        console.log('WebSocket connection established!');
        newWebSocket.send("{\"userName\":\"manbir\"}");
      };

      newWebSocket.onmessage = (event) => {
        const blob = event.data;
        const reader = new FileReader();
        reader.onload = function() {
          const message = reader.result;
          console.log(message);

          if (!message) {
            return;
          }

          const jsonObject = JSON.stringify(message);
          const newFiles = JSON.parse(jsonObject);

          setFiles(prevFiles => [...prevFiles, newFiles]);

          newWebSocket.close();
        }
        reader.readAsText(blob);
      };

      // Send the file then close
      // handleFileUpload();
      // handleClose();

        
    }, MINUTE_MS);

    return () => clearInterval(interval); // This represents the unmount function, in which you need to clear your interval to prevent memory leaks.
  }, [])

  const [searchTerm, setSearchTerm] = useState('');

  return (
    <div className="App">
      <div className='app_main'>
        <Navbar setSearchTerm={setSearchTerm}/>
        <div className='main_content'>
          <Sidebar/>
          <Files files={files} searchTerm={searchTerm}/>
          
        </div>
      </div>
    </div>
  );
}

export default App;