const constants = {
  appId: 'caad36a7-4d30-4880-9b4d-ab0a30501853',
  channelNamespace: 'urn:x-cast:com.colortv.testapp',
  status: {
    RUNNING: 'RUNNING',
    PAUSED: 'PAUSED'
  },
  senderMessages: {
    UP: 'MOVE_UP',
    LEFT: 'MOVE_LEFT',
    RIGHT: 'MOVE_RIGHT',
    DOWN: 'MOVE_DOWN',
    CENTER: 'MOVE_CENTER',
  },
  navigation: {
    DISCOVERY: {
      RIGHT: '#interstitial'
    },
    INTERSTITIAL: {
      LEFT: '#discovery',
      RIGHT: '#engagement'
    },
    ENGAGEMENT: {
      LEFT: '#interstitial',
      RIGHT: '#video'
    },
    VIDEO: {
      LEFT: '#engagement'
    },
  }
}

var color, castManager, channelConnection, selectedItem, status

window.addEventListener('ColorTVSDKReady', function () {
  'use strict';
  color = window.ColorTVSDK.getInstance()
  castManager = window.cast.receiver.CastReceiverManager.getInstance()
  status = constants.status.RUNNING
  color.init({
    appId: constants.appId
  }, function (error) {
    if (error) return
    initializeApplication()
  })
})

function initializeApplication () {
  channelConnection = castManager.getCastMessageBus(constants.channelNamespace)
  channelConnection.onMessage = onSenderMessage
  setSelected(document.querySelector('#discovery'))
  castManager.start()
}
function onSenderMessage (message) {
  if (status === constants.status.PAUSED) {
    return
  }
  switch (message.data) {
    case constants.senderMessages.UP:
      break
    case constants.senderMessages.LEFT:
      moveLeft()
      break
    case constants.senderMessages.RIGHT:
      moveRight()
      break
    case constants.senderMessages.DOWN:
      break
    case constants.senderMessages.CENTER:
      itemClick()
      break
    default:
      console.warn('UNRECOGNIZED:', message)
  }
}
function moveLeft () {
  var nextItem = getNextItem(selectedItem, 'LEFT')
  nextItem ?
    setSelected(document.querySelector(nextItem)) : false
}
function moveRight () {
  var nextItem = getNextItem(selectedItem, 'RIGHT')
  nextItem ?
    setSelected(document.querySelector(nextItem)) : false
}
function setSelected (domElement) {
  if (selectedItem) {
    selectedItem.classList.remove('active')
  }
  selectedItem = domElement
  selectedItem.classList.add('active')
}
function getNextItem (selectedItem, direction) {
  var actual = selectedItem.getAttribute('id')
  return constants.navigation[ actual.toUpperCase() ][ direction ]
}
function itemClick () {
  status = constants.status.PAUSED
  let clickedItem = selectedItem.getAttribute('id')
  switch (clickedItem) {
    case 'discovery':
      color.loadAd({
        placement: window.ColorTVSDK.Placements.APP_WALL
      }, function (error) {
        if (error) {
          return
        }
        color.showAd({
          placement: window.ColorTVSDK.Placements.APP_WALL,
          node: document.getElementById('insertAdsHere')
        }, function () {
          status = constants.status.RUNNING
        })
      })
      break
    case 'interstitial':
      color.loadAd({
        placement: window.ColorTVSDK.Placements.INTERSTITIAL
      }, function (error) {
        if (error) {
          return
        }
        color.showAd({
          placement: window.ColorTVSDK.Placements.INTERSTITIAL,
          node: document.getElementById('insertAdsHere')
        }, function () {
          status = constants.status.RUNNING
        })
      })
      break
    case 'engagement':
      color.loadAd({
        placement: window.ColorTVSDK.Placements.FULL_SCREEN
      }, function (error) {
        if (error) {
          return
        }
        color.showAd({
          placement: window.ColorTVSDK.Placements.FULL_SCREEN,
          node: document.getElementById('insertAdsHere')
        }, function () {
          status = constants.status.RUNNING
        })
      })
      break
    case 'video':
      color.loadAd({
        placement: window.ColorTVSDK.Placements.VIDEO
      }, function (error) {
        if (error) {
          return
        }
        color.showAd({
          placement: window.ColorTVSDK.Placements.VIDEO,
          node: document.getElementById('insertAdsHere')
        }, function () {
          status = constants.status.RUNNING
        })
      })
      break
  }
}
