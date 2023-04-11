import './App.css';
import Login from './components/Login/Login';
import Navbar from "./components/Navbar/Navbar";
import Files from "./components/Files/Files"
import Sidebar from "./components/Sidebar/Sidebar"
import { useState, useEffect, useRef } from 'react';
import { RESPONSE_QUEUE_IPS, REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT, RESPONSE_QUEUE_PORT, createWebSocket } from './components/WebSocket/WebSocket';
import { CircularProgress } from '@mui/material';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const MINUTE_MS = 1000;
  const [files, setFiles] = useState([]);
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [currentUser, setCurrentUser] = useState('')
  const [userName, setUsername] = useState('');
  const [loginStatus, setLoginStatus] = useState('');
  const [isLoading, setIsLoading] = useState(false);


  // Get the current title of the document
  const currentTitle = document.title;

  // Change the title of the document
  if (isLoggedIn){
    document.title = "Welcome to FileArc "+ currentUser + "!";
  }
  else{
    document.title = "Login to FileArc";
  }
  
  const updateUser=(u)=>{
    setCurrentUser(u);
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


  const handleSelectFile = (fileName, userName, shared, isSelected) => {
    console.log("IN IFFFFFFFFFFFFFF", fileName, userName, shared, isSelected);
    let newFile = {fileName: fileName, userName:userName ,shared: shared}
    if (isSelected) {
      setSelectedFiles([...selectedFiles, newFile]);
    } else {
      setSelectedFiles(selectedFiles.filter(file => file.fileName !== fileName));
    }
  };


  useEffect(() => {
    if (isLoggedIn){
      // setIsLoading(true);
      createWebSocket(REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT)
        .then((ws) => {
            console.log('WebSocket connection established:', ws);
            const payload = { requestType: "READ_ALL_FILES", currentUser: currentUser };
            ws.send(JSON.stringify(payload));
            ws.close();

        })
        .catch((error) => {
            console.error(`An error occurred while connecting to a WebSocket: ${error}`);
        });
      }
    }, [isLoggedIn]);
  
 

  const prevSelectedFiles = useRef([]);

  useEffect(() => {
    const interval = setInterval(() => {
      console.log('Logs every 2 secs');
      
      createWebSocket(RESPONSE_QUEUE_IPS, RESPONSE_QUEUE_PORT)
        .then((ws) => {
            console.log('WebSocket connection established:', ws);
            ws.send("{\"currentUser\":\"" + currentUser + "\"}");

            ws.onmessage = (event) => {
              const blob = event.data;
              const reader = new FileReader();
              reader.onload = function() {
                const message = reader.result;
      
                if (!message) {
                  return;
                }
                // const jsonString = JSON.stringify(message);
                const response = JSON.parse(message);
                console.log(response)
                if(response.responseType === "LOADALLFILES")
                {
                  setFiles(prevFiles => {
                    const updatedFiles = prevFiles.map(oldFile => {
                      const newFile = response.files.find(f => f.fileName === oldFile.fileName);
                      return newFile || oldFile;
                    });
                
                    response.files.forEach(newFile => {
                      if (!prevFiles.some(oldFile => oldFile.fileName === newFile.fileName)) {
                        updatedFiles.push(newFile);
                      }
                    });
                
                    return updatedFiles;
                  });
                  setIsLoading(false);
                }
                else if (response.responseType === "ALLFILESEMPTY"){
                    setIsLoading(false);
                }
                else if (response.responseType === "DOWNLOAD" || response.responseType === "UPDATE"){
                  setFiles(prevFiles => {
                    const updatedFiles = prevFiles.map(oldFile => {
                      if (oldFile.fileName === response.fileName) {
                        return response;
                      } else {
                        return oldFile;
                      }
                    });
                    
                    if (!prevFiles.some(oldFile => oldFile.fileName === response.fileName)) {
                      updatedFiles.push(response);
                    }

                    console.log("MY UPDATED FILES:", updatedFiles)
                    return updatedFiles;
                  });
                  
                  if(response.responseType === "DOWNLOAD"){
                    var fileURL = document.createElement("a"); //Create <a>
                    let fileType = response.fileName.split('.')[1]
                    fileURL.href = `data:application/${fileType};base64,${response.bytes}`
                    fileURL.download = response.fileName; //File name Here
                    fileURL.click(); //Downloaded file
                  }
                }
                else if(response.responseType === "DELETE"){
                    if (response.delete !== null){
                      const myArray = response.delete.split(",");
                      removeFiles(myArray);
                    }
                    else{
                      alert("No Files were deleted!");
                    }
                    // setDeletePressed(false);
                }
                else if (response.responseType === "REGISTER"){
                  if (response.registered === "SUCCESS"){
                    setUsername(response.currentUser);
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
                else if (response.responseType === "LOGIN"){
                  if (response.loggedIn === "SUCCESS"){
                    setUsername(response.currentUser);
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
                  setFiles(prevFiles => [...prevFiles, response]);
                }
              
              ws.close();
            }
            reader.readAsText(blob);
          };

        })
        .catch((error) => {
            console.error(`An error occurred while connecting to a WebSocket: ${error}`);
        });

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
          <Sidebar selectedFiles={selectedFiles} currentUser={currentUser} files={files}/>
          <Files files={files} searchTerm={searchTerm} handleSelectFile={handleSelectFile} currentUser={currentUser} />
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