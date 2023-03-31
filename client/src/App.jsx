import './App.css';
import Login from './components/Login/Login';
import Navbar from "./components/Navbar/Navbar";
import Files from "./components/Files/Files"
import Sidebar from "./components/Sidebar/Sidebar"
import { useState, useEffect, useRef } from 'react';
import { RESPONSE_QUEUE_SERVER_PORT } from './components/WebSocket/WebSocket';
import { WEBSOCKET_URL } from './components/WebSocket/WebSocket';
import { CircularProgress } from '@mui/material';


function createWebSocket(port) {
  return new WebSocket(port);
}


function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const MINUTE_MS = 5000;
  const [files, setFiles] = useState([]);
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [userName, setUsername] = useState('');
  const [loginStatus, setLoginStatus] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const updateUser = (newUser) => {
    setUsername(newUser);
  };

  // Get the current title of the document
  const currentTitle = document.title;

  // Change the title of the document
  if (isLoggedIn){
    document.title = "Welcome to the DFS "+ userName + "!";
  }
  else{
    document.title = "Welcome to the DFS!";
  }
  

  // Remove an array of files
  const removeFiles = (filesToRemove) => {

    console.log("RAJODE ", filesToRemove);
    setFiles(prevFiles => {
      // Filter out files to remove
      
      const newFiles = prevFiles.filter(file => !filesToRemove.includes(file.fileName));
      // Return new array of files
      return newFiles;
    });
  }

  useEffect(() => {
    console.log("REMOVED: " + files.fileName);
  }, [files]);


  const handleSelectFile = (id, isSelected) => {
    if (isSelected) {
      setSelectedFiles([...selectedFiles, id]);
    } else {
      setSelectedFiles(selectedFiles.filter(fileId => fileId !== id));
    }
  };

  useEffect(() => {
    console.log("RAGOD: " + selectedFiles);
  }, [selectedFiles]);


  useEffect(() => {
    if (isLoggedIn){
      // setIsLoading(true);
      const rqstSocket = createWebSocket(WEBSOCKET_URL)
      const payload = { requestType: "READ_ALL_FILES", userName: userName };
  

      rqstSocket.addEventListener('open', () => {
        console.log('RqstQ connection established!');

        // if(!flager){
        if (rqstSocket && rqstSocket.readyState === WebSocket.OPEN) {
            rqstSocket.send(JSON.stringify(payload));
            // flager = true
        }
          
        //}

        else {
            console.log("WEB SOCKET CONNECTION IS NOT OPEN!")
        }

        rqstSocket.close();
      
        });
      }
    }, [isLoggedIn]);
  
 

  const prevSelectedFiles = useRef([]);

  useEffect(() => {
    const interval = setInterval(() => {
      console.log('Logs every 2 secs');
      const newWebSocket = createWebSocket(RESPONSE_QUEUE_SERVER_PORT);
      
      newWebSocket.onopen = () => {
        console.log('WebSocket connection established!');
        
        newWebSocket.send("{\"userName\":\"" + userName + "\"}");
      };

      newWebSocket.onmessage = (event) => {
        const blob = event.data;
        const reader = new FileReader();
        reader.onload = function() {
          const message = reader.result;
          console.log("just receieved msg from rspoonseQ",message);

          if (!message) {
            return;
          }

          // const jsonString = JSON.stringify(message);
          const newFiles = JSON.parse(message);
          console.log("newfiles: ", typeof(newFiles));

          if(newFiles.responseType === "LOADALLFILES")
          {
            console.log("ARRAY");
            newFiles.files.forEach((item) => {
              console.log("the item is: ", item)
              setFiles(prevFiles=>[...prevFiles, item])
            });
            setIsLoading(false);
          }
          else{
            if (newFiles.responseType === "ALLFILESEMPTY"){
              setIsLoading(false);
            }
            else if (newFiles.responseType === "SINGLE"){
             
              const updatedFiles = files.map((file) => {
                console.log("FILE NAME 1: " + file.fileName);
                console.log("FILE NAME 2: " + newFiles.fileName);


                if (file.fileName === newFiles.fileName) {
                  return { ...file, bytes: newFiles.bytes };
                } else {
                  return file;
                }
              });

              console.log("Updated files: " + updatedFiles);
              setFiles(updatedFiles); 

              var fileURL = document.createElement("a"); //Create <a>
              let fileType = newFiles.fileName.split('.')[1]
              fileURL.href = `data:application/${fileType};base64,${newFiles.bytes}`
              fileURL.download = newFiles.fileName; //File name Here
              fileURL.click(); //Downloaded file
            }
            
            else if(newFiles.responseType === "DELETE"){
                console.log("received files to delete: " + newFiles.delete);
                const myArray = newFiles.delete.split(",");
                console.log("MY ARRAY: " + myArray[1]);

                removeFiles(myArray);

                // setDeletePressed(false);

            }

            else if (newFiles.responseType === "REGISTER"){

              if (newFiles.registered === "SUCCESS"){
                setLoginStatus('success');
                setIsLoggedIn(true);
                setIsLoading(false);
              }

              else{
                setLoginStatus('failed');
                setIsLoggedIn(false);
                setIsLoading(false);
              }
            }

            else if (newFiles.responseType === "LOGIN"){
              if (newFiles.loggedIn === "SUCCESS"){
                setLoginStatus('success');
                setIsLoggedIn(true);
                // setIsLoading(false);
              }

              else{
                setLoginStatus('failed');
                setIsLoggedIn(false);
                setIsLoading(false);
              }

            }

            else{
              setFiles(prevFiles => [...prevFiles, newFiles]);
            }
          }
          newWebSocket.close();
        }
        reader.readAsText(blob);
      };

      // Send the file then close
      // handleFileUpload();
      // handleClose();

        
    }, MINUTE_MS);

    return () => clearInterval(interval); // This represents the unmount function, in which you need to clear your interval to prevent memory leaks.
  }, useEffect(() => console.log("BLEH",files), [files]));

  const [searchTerm, setSearchTerm] = useState('');

  return (
    <div className="App">
      {isLoggedIn && !isLoading ? ( 
        <div className='app_main'>
          <Navbar setSearchTerm={setSearchTerm}/>
          <div className='main_content'>
          <Sidebar selectedFiles={selectedFiles} userName={userName} files={files}/>
          <Files files={files} searchTerm={searchTerm} handleSelectFile={handleSelectFile} userName={userName} />
          </div>
        </div>
      ) : isLoading ? (
        <div className='loader-container'>
          <CircularProgress />
        </div>
      ) : (
        <Login loginStatus={loginStatus} updateUser={updateUser} setIsLoading={setIsLoading}/>
      )}   
    </div>
  );
}

export default App;