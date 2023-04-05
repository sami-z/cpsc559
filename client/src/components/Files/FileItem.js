import React, { useState, useEffect } from 'react'
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import CropOriginalIcon from '@mui/icons-material/CropOriginal';
import './styles.css'
import { WEBSOCKET_URL } from '../WebSocket/WebSocket';
import { RESPONSE_QUEUE_SERVER_PORT } from '../WebSocket/WebSocket';
import { ownerDocument } from '@mui/material';
const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

function createWebSocket(port) {
  return new WebSocket(port);
}

const FileItem = ({ id, currentUser, onSelectFile, file }) => {
  const [isSelected, setIsSelected] = useState(false);


  const handleFileClick = () => {
    const socket = createWebSocket(WEBSOCKET_URL);
    socket.addEventListener('open', () => {
      console.log('RqstQ connection established!');

      if (socket && socket.readyState === WebSocket.OPEN) {

        const payload = { requestType: "DOWNLOAD", userName: currentUser, fileName: file.fileName }
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
      reader.onload = function () {
        const message = reader.result;

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
      }
    });
  };


  let caption = file.fileName
  let fileType = caption.split('.')[1]
  // let fileUrl = `data:application/${fileType};base64,${fileData}`
  const fileExtension = caption.split('.').pop();
  // const fileDate = `${timestamp?.toDate().getDate()} ${monthNames[timestamp?.toDate().getMonth() + 1]} ${timestamp?.toDate().getFullYear()}`
  let date = '04/04/2023'
  const dateParts = date.split('/');
  const fileDate = `${monthNames[dateParts[1] - 1]} ${parseInt(dateParts[0])}, ${dateParts[2]}`;

  const getReadableFileSizeString = (base64String) => {
    console.log("SAMIBYTES", base64String)
    const binaryData = atob(base64String);
    let fileSizeInBytes = binaryData.length;
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
          onSelectFile(file.fileName, file.ownerName, file.shared, !isSelected);
        }} />
        <div className="fileItem--left" onClick={handleFileClick}>
          {getIconByExtension(fileExtension)}
          <p>{caption}</p>
        </div>
        <div className="fileItem--right" onClick={handleFileClick}>
          <p>{file.shared}</p>
          <p>{file.ownerName === currentUser ? "me" : file.ownerName}</p>
          <p>{fileDate}</p>
          <p>{getReadableFileSizeString(file.bytes)}</p>
        </div>
      </a>
    </div>
  )
}

export default FileItem