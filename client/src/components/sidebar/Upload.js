import React, { useState } from 'react'
import AddIcon from '@mui/icons-material/Add'
import './styles.css'
import { REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT, createWebSocket } from '../WebSocket/WebSocket';


import { makeStyles } from '@mui/styles';
import Modal from '@mui/material/Modal';

function getModalStyle() {
    return {
        top: `50%`,
        left: `50%`,
        transform: `translate(-50%, -50%)`,
    };
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

        if (fileData == null) {
            return;
        }

        setUploadStatus('uploading');

        createWebSocket(REQUEST_QUEUE_IPS, REQUEST_QUEUE_PORT)
        .then((ws) => {
            console.log('WebSocket connection established:', ws);
            let shared = []
            let file = props.files.find(p => p.fileName === fileData.name);
            let userName = props.currentUser
            if (file) {
                console.log("FILE ALREADY EXISTS, KEEPING OLD SHARED ARR");
                shared = file.shared.split(",");
                userName = file.userName;

            }
            let today = new Date();
            let month = String(today.getMonth() + 1).padStart(2, '0');
            let day = String(today.getDate()).padStart(2, '0');
            let year = today.getFullYear();
            let date = month + '/' + day + '/' + year;
            console.log("JAKE WAKE", date)
            const payload = { requestType: "WRITE", currentUser: props.currentUser, userName: userName, fileName: fileData.name, fileType: fileData.type, bytes: fileBytes, shared: shared, created: date };
            console.log("upload request payload", payload)
            ws.send(JSON.stringify(payload));

            setTimeout(() => {
                setUploadStatus('uploaded');
                setOpen(false);
                setFileData(null);
                setFileBytes(null);
                setUploadStatus('idle');
            }, 2500); // add a 2.5 second delay
            ws.close();

            

        })
        .catch((error) => {
            console.error(`An error occurred while connecting to a WebSocket: ${error}`);
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
