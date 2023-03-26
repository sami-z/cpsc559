import React, { useState, useEffect } from 'react'
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import CropOriginalIcon from '@mui/icons-material/CropOriginal';
import './styles.css'
const monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];



const FileItem = ({ id, caption, timestamp, size, onSelectFile}) => {
  console.log("id, caption, timestamp, fireurl, size", size)
  const [isSelected, setIsSelected] = useState(false);

     

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
    <div className='fileItem'>
      <a  target="_blank" download={caption}>
        {/* <input type="checkbox" checked={isSelected} onChange={() => setIsSelected(!isSelected)} /> */}
        <input type="checkbox" checked={isSelected} onChange={() => {
            setIsSelected(prevState => !prevState);
            onSelectFile(caption, !isSelected);
        }} />
        <div className="fileItem--left">
          {getIconByExtension(fileExtension)}
          <p>{caption}</p>
        </div>
        <div className="fileItem--right">
          <p>{fileDate}</p>
          <p>{getReadableFileSizeString(size)}</p>
        </div>
      </a>
    </div>
  )
}

export default FileItem