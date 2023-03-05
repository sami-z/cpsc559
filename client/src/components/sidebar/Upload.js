import React, {useState} from 'react'
import AddIcon from '@mui/icons-material/Add'
import './Upload.css'
import { WEBSOCKET_URL } from '../WebSocket/WebSocket';


import { makeStyles } from '@mui/styles';
import Modal from '@mui/material/Modal';

function getModalStyle() {
    return {
        top: `50%`,
        left: `50%`,
        transform: `translate(-50%, -50%)`,
    };
}

function createWebSocket() {
    return new WebSocket(WEBSOCKET_URL);
  }

const useStyles = makeStyles({
    root: {
        position: 'absolute',
        background: 'white',
        border: '2px solid #000',
        borderRadius: 3,
        color: 'black',
        height: 48,
        padding: '0 30px',
    },
  });

function Upload(props) {
    const classes = useStyles();

    const [modalStyle] = useState(getModalStyle);
    const [open, setOpen] = useState(false);
    const [fileData, setFileData] = useState(null);
    const [fileBytes, setFileBytes] = useState(null);
    const [uploading, setUploading] = useState(false);

    const handleSend = () => {
        
    // public String requestType;
    // public String userName;
    // public String fileName;
    // public String fileType;
    // public String bytes;
    // public String shareWith;


        const newWebSocket = createWebSocket();
        const payload = { requestType: "WRITE", userName: "manbir", fileName: fileData.name, fileType: fileData.type, bytes: fileBytes, shareWith: null};

        newWebSocket.addEventListener('open', () => {
            console.log('WebSocket connection established!');

            console.log(newWebSocket);

            if ( newWebSocket && newWebSocket.readyState === WebSocket.OPEN) {
                newWebSocket.send(JSON.stringify(payload));
            }

    
            else{
                console.log("WEB SOCKET CONNECTION IS NOT OPEN!")
            }

            setOpen(false)
            setFileData(null)
            setFileBytes(null)

            newWebSocket.close()
            console.log(newWebSocket);

            // Send the file then close
            // handleFileUpload();
            // handleClose();
        });

    };

    const handleOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    const handleChange = (e) => {
        if (e.target.files[0]) {
            const file = e.target.files[0];
            const reader = new FileReader();
            reader.onload = (e) => {
                setFileData(file);
                const base64Data = e.target.result.split(",")[1]; 
                setFileBytes(base64Data);
                console.log("File to upload", file);
                console.log("File bytes", base64Data);
            };

            reader.readAsDataURL(file);
        }
    }


  return (
    <div className='upload'>
        <div className='upload__container' onClick={handleOpen}>
            <AddIcon/>
            <p>Upload</p>
        </div>

        <Modal
                open={open}
                onClose={handleClose}
                aria-labelledby="simple-modal-title"
                aria-describedby="simple-modal-description"
            >

                {/* // {classes.paper} */}
                <div style={modalStyle} className={classes.root}>
                    <center>
                        <p>Select file(s) to upload:</p>
                    </center>
                    {
                        uploading ? (
                            <p>Uploading...</p>
                        ) : (
                                <>
                                    <input type="file" onChange={handleChange} />
                                    <button onClick={handleSend}>Upload to DFS</button>
                                    {/* <button onClick={handleFileUpload}>Upload to DFS</button> */}
                                    {/* {fileData && <p>{fileData}</p>} */}
                                </>
                            )
                    }
                </div>
            </Modal>
    </div>
  )
}

export default Upload
