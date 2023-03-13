import './App.css';
import Navbar from "./components/Navbar/Navbar";
import Files from "./components/Files/Files"
import Sidebar from "./components/Sidebar/Sidebar"
import { useState, useEffect } from 'react';
import { RESPONSE_QUEUE_SERVER_PORT } from './components/WebSocket/WebSocket';
import { WEBSOCKET_URL } from './components/WebSocket/WebSocket';
import { typeOf } from 'react-is';

let flager = false
function createWebSocket(port) {
  return new WebSocket(port);
}

function App() {
  console.log("Start of App.jsx")
  const MINUTE_MS = 5000;
  const [files, setFiles] = useState([]);
  
  const rqstSocket = createWebSocket(WEBSOCKET_URL)
  const payload = { requestType: "READ", userName: "manbir", readType: "allFiles" };
  

  rqstSocket.addEventListener('open', () => {
      console.log('RqstQ connection established!');

      console.log(rqstSocket);

      if(!flager){
        if (rqstSocket && rqstSocket.readyState === WebSocket.OPEN) {
            rqstSocket.send(JSON.stringify(payload));
            flager = true
        }
        
      }


      else {
          console.log("WEB SOCKET CONNECTION IS NOT OPEN!")
      }


      rqstSocket.close();
      

  });

  useEffect(() => {
    const interval = setInterval(() => {
      console.log('Logs every 2 secs');
      const newWebSocket = createWebSocket(RESPONSE_QUEUE_SERVER_PORT);
      
      newWebSocket.onopen = () => {
        console.log('WebSocket connection established!');
        
        newWebSocket.send("{\"userName\":\"manbir\"}");
      };

      newWebSocket.onmessage = (event) => {
        const blob = event.data;
        const reader = new FileReader();
        reader.onload = function() {
          const message = reader.result;
          console.log("just receieved msg from rspoonseQ",message);

          // if (!message || !message.includes("fileName")) {
          //   return;
          // }
          if (!message) {
            return;
          }

          // const jsonString = JSON.stringify(message);
          const newFiles = JSON.parse(message);
          console.log("newfiles: ", typeof(newFiles));
          if(Array.isArray(newFiles))
          {
            newFiles.forEach((item) => {
              console.log("the item is: ", item)
              setFiles(prevFiles=>[...prevFiles, item])
            });
          }
          else{
            console.log("received single file");
            setFiles(prevFiles => [...prevFiles, newFiles]);
          }
          console.log("SAMI FILES", files)
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