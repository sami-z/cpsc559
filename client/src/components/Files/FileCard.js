import React from 'react';
import './styles.css';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import CropOriginalIcon from '@mui/icons-material/CropOriginal';
import { WEBSOCKET_URL } from '../WebSocket/WebSocket';
import { RESPONSE_QUEUE_SERVER_PORT } from '../WebSocket/WebSocket';
import { create } from '@mui/material/styles/createTransitions';

const FileCard = (props) => {
  const fileExtension = props.name.split('.').pop();

  const getIconByExtension = (fileName) => {
    const extension = fileName.split('.').pop();
    switch (extension.toLowerCase()) {
      case 'pdf':
        return <PictureAsPdfIcon style={{ fontSize: 130 }} />;
      case 'png':
        return <CropOriginalIcon style={{ fontSize: 130 }} />;
      case 'jpg':
        return <CropOriginalIcon style={{ fontSize: 130 }} />;
      case 'jpeg':
        return <CropOriginalIcon style={{ fontSize: 130 }} />;
      default:
        return <InsertDriveFileIcon style={{ fontSize: 130 }} />;
    }
  };

  function createWebSocket(port) {
    return new WebSocket(port);
  }

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

  const handleCardClick = () => {
    const socket = createWebSocket(WEBSOCKET_URL);
    socket.addEventListener('open', () => {
      console.log('RqstQ connection established!');

      if (socket && socket.readyState === WebSocket.OPEN) {

        const payload = { requestType: "DOWNLOAD", userName: props.currentUser, fileName: props.name }
        console.log(payload);
        socket.send(JSON.stringify(payload));
      }

    });

    const responseSocket = createWebSocket(RESPONSE_QUEUE_SERVER_PORT);
    responseSocket.addEventListener('message', (event) => {
      console.log("Logging event: " + event);
      console.log("Logging event data: " + event.data);
      const { responseType, data } = JSON.parse(event.data);

      const blob = event.data;
      const reader = new FileReader();
      reader.onload = function () {
        const message = reader.result;
        console.log("just receieved msg from rspoonseQ", message);

        if (!message) {
          return;
        }

        const newFiles = JSON.parse(message);
        const bytes = new Uint8Array(data);
        const blob = new Blob([bytes]);
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.target = '_blank';
        a.click();
        URL.revokeObjectURL(url);
        console.log("I BE IN HERE");
      }
    });
  };


  return (
    <div className='fileCard' onClick={handleCardClick}>
      <div className="fileCard--top">
        {getIconByExtension(fileExtension)}
      </div>

      <div className="fileCard--bottom">
        <p>{props.name}</p>
      </div>
    </div>
  )
}

export default FileCard