import React, { useState, useEffect } from 'react'
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import CropOriginalIcon from '@mui/icons-material/CropOriginal';
import './styles.css'
import { WEBSOCKET_URL } from '../WebSocket/WebSocket';
import { RESPONSE_QUEUE_SERVER_PORT } from '../WebSocket/WebSocket';
const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

function createWebSocket(port) {
  return new WebSocket(port);
}

const FileItem = ({ id, caption, timestamp, size, onSelectFile, userName}) => {
  console.log("id, caption, timestamp, fireurl, size", size)
  const [isSelected, setIsSelected] = useState(false);


  const handleFileClick = () => {
    const socket = createWebSocket(WEBSOCKET_URL);
    socket.addEventListener('open', () => {
      console.log('RqstQ connection established!');

      if (socket && socket.readyState === WebSocket.OPEN) {

        const payload = {requestType: "READ", readType: "SINGLE", userName: userName, fileName: caption}
        console.log("FILE I WANT TO DOWNLOAD: " + JSON.stringify(payload));
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
      reader.onload = function() {
        const message = reader.result;
        console.log("just receieved msg from rspoonseQ",message);
  
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

     

  let fileType = caption.split('.')[1]
  // let fileUrl = `data:application/${fileType};base64,${fileData}`
  console.log("In FileItem.js, ", caption, timestamp, size);
  const fileExtension = caption.split('.').pop();
  //const fileDate = `${timestamp?.toDate().getDate()} ${monthNames[timestamp?.toDate().getMonth() + 1]} ${timestamp?.toDate().getFullYear()}`
  const fileDate = "09/03/2023"
  const getReadableFileSizeString = (fileSizeInBytes) => {
    let i = -1;
    const byteUnits = [' kB', ' MB', ' GB', ' TB', 'PB', 'EB', 'ZB', 'YB'];
    do {
      fileSizeInBytes = fileSizeInBytes / 1024;
      i++;
    } while (fileSizeInBytes > 1024);

    return Math.max(fileSizeInBytes, 0.1).toFixed(1) + byteUnits[i];
  };

  const getIconByExtension = (fileName) => {
    const extension = fileName.split('.').pop();
    switch (extension.toLowerCase()) {
      case 'pdf':
        return <PictureAsPdfIcon />;
      case 'png':
        return <CropOriginalIcon />;
      case 'jpg':
        return <CropOriginalIcon />;
      case 'jpeg':
        return <CropOriginalIcon />;
      default:
        return <InsertDriveFileIcon />;
    }
  };

  return (
    <div className={isSelected ? 'highlight' : 'fileItem'}>
      <a>
        {/* <input type="checkbox" checked={isSelected} onChange={() => setIsSelected(!isSelected)} /> */}
        <input type="checkbox" checked={isSelected} onChange={() => {
            setIsSelected(prevState => !prevState);
            onSelectFile(caption, !isSelected);
        }} />
        <div className="fileItem--left" onClick={handleFileClick}>
          {getIconByExtension(fileExtension)}
          <p>{caption}</p>
        </div>
        <div className="fileItem--right" onClick={handleFileClick}>
          <p>{fileDate}</p>
          <p>{getReadableFileSizeString(size)}</p>
        </div>
        </a>
    </div>
  )
}

export default FileItem