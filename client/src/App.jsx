import './App.css';
import Navbar from "./components/Navbar/Navbar";
import Files from "./components/Files/Files"
import Sidebar from "./components/Sidebar/Sidebar"
import { useState, useEffect } from 'react';
import { RESPONSE_QUEUE_SERVER_PORT } from './components/WebSocket/WebSocket';
import { WEBSOCKET_URL } from './components/WebSocket/WebSocket';
import './Login.css';
import PersonIcon from '@mui/icons-material/Person';
import KeyIcon from '@mui/icons-material/Key';
import { Input, InputAdornment } from '@mui/material';



let flager = false

function createWebSocket(port) {
  return new WebSocket(port);
}

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = () => {
    const enteredUsername = username.trim(); 
    const enteredPass = password.trim()
    console.log("Entered username for login: ", enteredUsername);
    console.log("Entered password for login ", enteredPass);

    const newWebSocket = createWebSocket(WEBSOCKET_URL);
    const payload = { requestType: "LOGIN", userName: enteredUsername, password: enteredPass };
        
        newWebSocket.addEventListener('open', () => {
            console.log('WebSocket connection established!');

            console.log(newWebSocket);


            if (newWebSocket && newWebSocket.readyState === WebSocket.OPEN) {
                newWebSocket.send(JSON.stringify(payload));
            }


            else {
                console.log("WEB SOCKET CONNECTION IS NOT OPEN!")
            }

            // setTimeout(() => {
            //     setUploadStatus('uploaded');
            //     setOpen(false);
            //     setFileData(null);
            //     setFileBytes(null);
            //     setUploadStatus('idle');
            // }, 2500); // add a 2.5 second delay
            newWebSocket.close();

        });


    
  };

  const handleRegister = () => {
    const enteredUsername = username.trim(); 
    const enteredPass = password.trim()
    console.log("Entered username for register: ", enteredUsername);
    console.log("Entered password for register ", enteredPass);
  };

  return (
    <div className="login">
      <h1>Login or Register</h1>
      <h4 className="subheading">to continue to Distributed File System</h4>
      <form>
        <div className="form-group">
          <Input
            type="text"
            className="input-field"
            placeholder="Username"
            id="username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            startAdornment={
              <InputAdornment position="start">
                <PersonIcon />
              </InputAdornment>
            }
          />
        </div>

        <div className="form-group">
          <Input
            type="password"
            className="input-field"
            placeholder="Password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            startAdornment={
              <InputAdornment position="start">
                <KeyIcon/>
              </InputAdornment>
            }
          />
        </div>

        <div className="button-group">
          <button type="button" className="login-button buttons-for-login" onClick={handleLogin}>
            Login
          </button>
          <button type="button" className="register-button buttons-for-login" onClick={handleRegister}>
            Register
          </button>
        </div>
      </form>
    </div>
  );
}


function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const MINUTE_MS = 5000;
  const [files, setFiles] = useState([]);
  
  // const rqstSocket = createWebSocket(WEBSOCKET_URL)
  // const payload = { requestType: "READ", userName: "manbir", readType: "allFiles" };
  

  // rqstSocket.addEventListener('open', () => {
  //     console.log('RqstQ connection established!');

  //     console.log(rqstSocket);

  //     if(!flager){
  //       if (rqstSocket && rqstSocket.readyState === WebSocket.OPEN) {
  //           rqstSocket.send(JSON.stringify(payload));
  //           flager = true
  //       }
        
  //     }


  //     else {
  //         console.log("WEB SOCKET CONNECTION IS NOT OPEN!")
  //     }


  //     rqstSocket.close();
      

  // });

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
      {true ? ( //replace with: isLoggedIn
        <div className='app_main'>
          <Navbar setSearchTerm={setSearchTerm}/>
          <div className='main_content'>
            <Sidebar/>
            <Files files={files} searchTerm={searchTerm}/>
          </div>
        </div>
      ) : (
        <Login />
      )}   
    </div>
  );
}

export default App;