import React from 'react';
import './styles.css';
import InsertDriveFileIcon from '@mui/icons-material/InsertDriveFile';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import CropOriginalIcon from '@mui/icons-material/CropOriginal';

const FileCard = ({ name }) => {

    const fileExtension = name.split('.').pop();

    const getIconByExtension = (fileName) => {
        const extension = fileName.split('.').pop();
        switch (extension.toLowerCase()) {
          case 'pdf':
            return <PictureAsPdfIcon style={{ fontSize: 130 }}/>;
          case 'png':
            return <CropOriginalIcon style={{ fontSize: 130 }}/>;
          case 'jpg':
            return <CropOriginalIcon style={{ fontSize: 130 }}/>;
          case 'jpeg':
            return <CropOriginalIcon style={{ fontSize: 130 }}/>;
          default:
            return <InsertDriveFileIcon style={{ fontSize: 130 }}/>;  
        }
      };


    return (
        <div className='fileCard'>
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