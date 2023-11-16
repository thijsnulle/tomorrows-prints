const IMAGES_TO_SELECT_JSON_FILE = 'json/until-161123.json';

const addImageToSelectedContainer = (image, key) => {
    const containerId = (key === "Enter") ? "yes-selection-container" : "no-selection-container";
    const selectionContainer = document.getElementById(containerId);

    const imageElement = document.createElement('img');
    imageElement.src = image['url'];

    const removeImageElement = () => selectionContainer.querySelector('.image-container').removeChild(imageElement);
    imageElement.addEventListener('click', removeImageElement);

    const imageContainer = selectionContainer.querySelector('.image-container');
    imageContainer.insertBefore(imageElement, imageContainer.firstChild);
};

const fetchImages = (url) => {
    let json = [];
    $.ajax({
        'async': false,
        'global': false,
        'url': url,
        'dataType': "json",
        'success': function (data) {
            json = data;
        }
    });
    return json;
};

const selectedImages = fetchImages('json/selected-images.json');
const notSelectedImages = fetchImages('json/not-selected-images.json');

const processImageContainers = (image, frameUrls) => {
    return frameUrls.map((frameUrl) => {
        const imageElement = document.createElement('img');
        imageElement.src = image['url'];
        imageElement.setAttribute('class', 'print');

        const frameContainer = document.createElement('div');
        frameContainer.setAttribute('class', 'frame-container');
        frameContainer.appendChild(imageElement);

        const frameImage = document.createElement('img');
        frameImage.src = frameUrl;
        frameImage.setAttribute('class', 'frame');

        frameContainer.appendChild(frameImage);
        return frameContainer;
    });
};

const displayFrames = (frameContainers) => {
    const selectionContainer = document.getElementById('selection-container');
    selectionContainer.innerHTML = "";
    frameContainers.forEach((frameContainer) => selectionContainer.appendChild(frameContainer));
};

const updateTitle = (aspectRatio) => {
    const title = document.getElementById('title');
    if (Math.abs(aspectRatio - (2 / 3)) > 0.01) {
        title.classList = ['wrong-aspect-ratio'];
        title.innerHTML = `Image Selection (2:${(2 / aspectRatio)})`;
    } else {
        title.classList = [];
        title.innerHTML = 'Image Selection';
    }
};

const startSelection = (images) => {
    if (images.length === 0) {
        alert('No more images to select.');
        return;
    }

    const image = images.pop();
    const frameUrls = ['images/black-frame.png', 'images/dark-brown-frame.png', 'images/brown-frame.png', 'images/light-brown-frame.png'];
    
    const frameContainers = processImageContainers(image, frameUrls);
    displayFrames(frameContainers);

    const imageObject = new Image();
    imageObject.src = image['url'];

    imageObject.onload = () => {
        const aspectRatio = imageObject.width / imageObject.height;
        updateTitle(aspectRatio);
    };

    const eventHandler = (e) => {
        if (e.key === "Enter" || e.key === "Escape") {
            addImageToSelectedContainer(image, e.key);

            if (e.key === "Enter") selectedImages.unshift(image);
            if (e.key === "Escape") notSelectedImages.unshift(image);

            document.removeEventListener('keypress', eventHandler);
            startSelection(images);
        }
    };

    document.addEventListener('keypress', eventHandler);
};

$.getJSON(IMAGES_TO_SELECT_JSON_FILE, (images) => {
    const selectedImageURLs = selectedImages.map((img) => img['url']);
    const notSelectedImageURLs = notSelectedImages.map((img) => img['url']);
    const processedImageURLs = new Set(selectedImageURLs.concat(notSelectedImageURLs));

    const imagesToProcess = images.filter((image) => !processedImageURLs.has(image['url']));
    startSelection(imagesToProcess);
});

const exportImages = () => {
    const downloadImage = (fileName, imageArray) => {
        const imageJSON = JSON.stringify(imageArray, null, 2);
        $("<a />", { "download": fileName, "href": "data:application/json," + encodeURIComponent(imageJSON) })
            .appendTo("body").click(function () { $(this).remove() })[0].click();
    };

    downloadImage('selected-images.json', selectedImages);
    setTimeout(() => downloadImage('not-selected-images.json', notSelectedImages), 100);
};

document.getElementById("export-button").addEventListener('click', exportImages);

document.addEventListener('keypress', (e) => {
    if (e.code === "Space") {
        const selectionContainer = document.getElementById("selection-container");
        selectionContainer.classList.toggle('full-size');
    }
});
