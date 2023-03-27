import React, { useState } from 'react'
import DeleteIcon from '@mui/icons-material/Delete';
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
        padding: '0 30px',
    },
});

function Delete(props) {
    const classes = useStyles();


    const [uploadStatus, setUploadStatus] = useState('idle'); // 'idle', 'uploading', 'uploaded'
    const [modalStyle] = useState(getModalStyle);
    const [open, setOpen] = useState(false);
    const [fileData, setFileData] = useState(null);
    const [fileBytes, setFileBytes] = useState(null);
    const [uploading, setUploading] = useState(false);

    const handleDelete = async () => {


        console.log("IN HANDLE DELETE: ", props.selectedFiles);

        setUploadStatus('uploading');
        const newWebSocket = createWebSocket();
        const payload = { requestType: "WRITE", userName: props.userName, writeType: "DELETE", filesToDelete: props.selectedFiles, shareWith: null };
        
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

    // const handleChange = (e) => {
    //     // setUploadStatus('uploading');
    //     if (e.target.files[0]) {
    //         const file = e.target.files[0];
    //         const reader = new FileReader();
    //         reader.onload = (e) => {
    //             setFileData(file);
    //             const base64Data = e.target.result.split(",")[1];
    //             setFileBytes(base64Data);
    //             console.log("File to upload", file);
    //             console.log("File bytes", base64Data);
    //         };

    //         reader.readAsDataURL(file);
    //     }
    // }


    return (
        <div className='upload'>
            <div className='upload__container' onClick={handleOpen}>
                <DeleteIcon />
                <p className='side-button-container'>Delete</p>
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
                        <p>Delete the selected file(s)?:</p>
                    </center>
                    {
                        uploadStatus === 'uploading' ? (
                            <div>
                                <center>
                                    <p>Deleting...</p>
                                    <div className="loading-spinner"></div>
                                </center>

                            </div>

                        ) : (
                            <>
                                {/* <input type="select" onChange={handleChange} /> */}
                                <center>
                                    <button onClick={handleDelete}>Confirm</button>
                                </center>
                            </>
                        )
                    }
                </div>
            </Modal>
        </div>
    )
}

export default Delete
