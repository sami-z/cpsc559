import React from 'react';
import './styles.css';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import CropOriginalIcon from '@mui/icons-material/CropOriginal';
import { WEBSOCKET_URL } from '../WebSocket/WebSocket';

const FileCard = ({ name }) => {
  const fileExtension = name.split('.').pop();

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

        const payload = {requestType: "READ", readType: "SINGLE", userName: "manbir", fileName: name}
        console.log("FILE I WANT TO DOWNLOAD: " + name);
        socket.send(JSON.stringify(payload));          
      }

    });
  };


  return (
    <div className='fileCard' onClick={handleCardClick}>
      <div className="fileCard--top">
        {getIconByExtension(fileExtension)}
      </div>

      <div className="fileCard--bottom">
        <p>{name}</p>
      </div>
    </div>
  )
}

export default FileCard