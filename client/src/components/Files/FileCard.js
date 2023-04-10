import React from 'react';
import './styles.css';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import CropOriginalIcon from '@mui/icons-material/CropOriginal';
import { REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT, createWebSocket } from '../WebSocket/WebSocket';

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

 
  const handleCardClick = () => {
    createWebSocket(REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT)
        .then((ws) => {
            console.log('WebSocket connection established:', ws);
            const payload = { requestType: "DOWNLOAD", ownerName: props.ownerName, fileName: props.name, currentUser: props.currentUser }
            ws.send(JSON.stringify(payload));
            ws.close();

        })
        .catch((error) => {
            console.error(`An error occurred while connecting to a WebSocket: ${error}`);
        });
  };


  return (
    <div className='fileCard' onDoubleClick={handleCardClick}>
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