import React, { useState, useEffect } from 'react'
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import CropOriginalIcon from '@mui/icons-material/CropOriginal';
import './styles.css'
import { REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT, createWebSocket } from '../WebSocket/WebSocket';

const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];


const FileItem = ({ id, currentUser, onSelectFile, file }) => {
  const [isSelected, setIsSelected] = useState(false);

  const handleFileClick = () => {

    createWebSocket(REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT)
        .then((ws) => {
            console.log('WebSocket connection established:', ws);
            const payload = { requestType: "DOWNLOAD", ownerName: file.userName, fileName: file.fileName, currentUser: currentUser }
            ws.send(JSON.stringify(payload));
            ws.close();

        })
        .catch((error) => {
            console.error(`An error occurred while connecting to a WebSocket: ${error}`);
        });
  };


  let caption = file.fileName
  let fileType = caption.split('.')[1]
  const fileExtension = caption.split('.').pop();
  const dateParts = file.created.split('/');
  const fileDate = `${monthNames[dateParts[1] - 1]} ${parseInt(dateParts[0])}, ${dateParts[2]}`;

  const getReadableFileSizeString = (base64String) => {
    const byteUnits = [' kB', ' MB', ' GB', ' TB', 'PB', 'EB', 'ZB', 'YB'];
    if (base64String == null){
      return 0 + byteUnits[0]
    }
    const binaryData = atob(base64String);
    let fileSizeInBytes = binaryData.length;
    let i = -1;
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
        <input type="checkbox" checked={isSelected} onChange={() => {
          setIsSelected(prevState => !prevState);
          onSelectFile(file.fileName, file.userName, file.shared, !isSelected);
        }} />
        <div className="fileItem--left" onDoubleClick={handleFileClick}>
          {getIconByExtension(fileExtension)}
          <p>{caption}</p>
        </div>
        <div className="fileItem--right" onDoubleClick={handleFileClick}>
          <p>{file.shared.length === 0 ? "None" : file.shared.join(", ")}</p>
          <p>{file.userName === currentUser ? "Me" : file.userName}</p>
          <p>{fileDate}</p>
          <p>{getReadableFileSizeString(file.bytes)}</p>
        </div>
      </a>
    </div>
  )
}

export default FileItem