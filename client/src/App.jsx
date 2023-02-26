import './App.css';
import Navbar from "./components/Navbar/Navbar";
import Files from "./components/Files/Files"
import Sidebar from "./components/sidebar"
import { useState, useEffect } from 'react';

const URL = "ws://localhost:8080";

const webSocket = new WebSocket(URL);



 // useEffect(() => {
    //   return () => {
    //     webSocket.close();
    //   };
    // }, []);


webSocket.onopen = () => {
    console.log('Connected to WebSocket');
  };
  
webSocket.onclose = () => {
    console.log('Disconnected from WebSocket');
  };
  
  webSocket.onmessage = (event) => {
    console.log('Received message from WebSocket:', event.data);
  };


function App() {
  return (
    <div className="App">
      <Navbar/>
      <Sidebar myProp={webSocket}/>
      <Files/>
    </div>
  );
}

export default App;
