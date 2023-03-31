import React, { useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import './styles.css'
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
        padding: '0 10px',
    },
});

function Upload(props) {
    const classes = useStyles();


    const [uploadStatus, setUploadStatus] = useState('idle'); // 'idle', 'uploading', 'uploaded'
    const [modalStyle] = useState(getModalStyle);
    const [open, setOpen] = useState(false);
    const [fileData, setFileData] = useState(null);
    const [fileBytes, setFileBytes] = useState(null);
    const [uploading, setUploading] = useState(false);

    const handleSend = async () => {

        console.log("BEGINING HANDLE SEND")

        if (fileData == null) {
            return;
        }

        console.log("END HANDLE SEND")

        setUploadStatus('uploading');
        const newWebSocket = createWebSocket();
        console.log("THIS IS THE USERNAME IS DELETE: " + props.userName)
        let shareWith = []
        let userName = props.userName
        console.log("INUPLOAD FILES", props.files);
        let file = props.files.find(p => p.fileName === fileData.name);
        if (file) {
            console.log("FILE ALREADY EXISTS, KEEPING OLD SHAREWITH");
            shareWith = file.shared.split(",");
            userName = file.userName;

        }

        const payload = { requestType: "WRITE", userName: userName, fileName: fileData.name, fileType: fileData.type, bytes: fileBytes, shareWith: shareWith };

        newWebSocket.addEventListener('open', () => {
            console.log('WebSocket connection established!');

            console.log(newWebSocket);


            if (newWebSocket && newWebSocket.readyState === WebSocket.OPEN) {
                newWebSocket.send(JSON.stringify(payload));
            }


            else {
                console.log("WEB SOCKET CONNECTION IS NOT OPEN!")
            }

            setTimeout(() => {
                setUploadStatus('uploaded');
                setOpen(false);
                setFileData(null);
                setFileBytes(null);
                setUploadStatus('idle');
            }, 2500); // add a 2.5 second delay


            newWebSocket.close();

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

    function handleBlur() {
        console.log("IN HANDLE BLUR")
        setFileData(null);
        handleClose();
        // reset any other values as needed
    }


    return (
        <div className='upload'>
            <div className='upload__container' onClick={handleOpen}>
                <AddIcon />
                <p className='side-button-container'>Upload</p>
            </div>

            <Modal
                open={open}
                onClose={handleBlur}
                aria-labelledby="simple-modal-title"
                aria-describedby="simple-modal-description"
            >

                {/* // {classes.paper} */}
                <div style={modalStyle} className={classes.root}>
                    <center>
                        <p>Select file(s) to upload:</p>
                    </center>
                    {
                        uploadStatus === 'uploading' ? (
                            <div>
                                <center>
                                    <p>Uploading...</p>
                                    <div className="loading-spinner"></div>
                                </center>

                            </div>

                        ) : (
                            <>
                                <input type="file" onChange={handleChange} />
                                <button onClick={handleSend}>Upload to DFS  </button>
                            </>
                        )
                    }
                </div>
            </Modal>
        </div>
    )
}

export default Upload
