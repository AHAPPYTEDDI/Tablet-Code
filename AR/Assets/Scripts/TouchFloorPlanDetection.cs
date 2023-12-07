using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.XR.ARFoundation;

public class TouchFloorPlanDetection : MonoBehaviour
{
    public ARRaycastManager arSessionOrigin;

    public ARPlaneManager arPlaneManager;

    public List<ARRaycastHit> hits;

    private float yChosenPosition;

    public bool yPositionFound;

    private bool areFeetDisplayed;

    [SerializeField]
    public ScannerBLE scanner;

    [SerializeField]
    public GameObject leftFootPrefab;

    [SerializeField]
    public GameObject rightFootPrefab;

    private float footSeparation = 0.32f;

    // Starting Position of Foot
    private Vector3 camStartPos;
    private float zStartPosition = 0.5f;
    private float newPos = 0.5f;

    private Vector3 localRightFootOffset;
    private Vector3 localLeftFootOffset;

    private GameObject rightFootInGame;
    private GameObject leftFootInGame;

    private Vector3 cameraXZPos;
    private Quaternion viewRot;

    private bool isLeft;

    [SerializeField]
    private GameObject instructionPanel;

    private bool instructionPanelActive;

    public GameObject placedPrefab;

    private bool isOtherVisualizationActive;

    public GameObject VisualizationButton;


    // Start is called before the first frame update
    void Start()
    {
        //Initialise list for Raycast hits
        hits = new List<ARRaycastHit>();

        //Get AR Session Origin and AR PlaneManager

        arSessionOrigin = GetComponent<ARRaycastManager>();
        arPlaneManager = GetComponent<ARPlaneManager>();

        //Determines whether screen has been touched to find yPosition of plane
        yPositionFound = false;

    }


    // Update is called once per frame
    void Update()
    {
        isOtherVisualizationActive = VisualizationButton.GetComponent<ChangeViz>().areFeetActive;

        if (!isOtherVisualizationActive)
        {
            GetTouchInput();
            DisplayFeet();
            MoveFeet();
            //UpdateFeet();
        }
    }

    public void GetTouchInput()
    {
        instructionPanelActive = instructionPanel.GetComponent<InstructionsController>().panelOn;
        if (instructionPanelActive == false)
        {
            // if screen is touched
            if (Input.touchCount > 0)
            {
                Touch touch = Input.GetTouch(0);
                if (touch.phase == TouchPhase.Began)
                {
                    if (arSessionOrigin.Raycast(touch.position, hits))
                    {
                        if (!yPositionFound)
                        {
                            // find position of touch
                            Pose hitPose = hits[0].pose;

                            //allocate y position of touched plane to variable
                            yChosenPosition = hitPose.position.y;

                            // Marker to pass to other script to initialise feet model
                            yPositionFound = true;


                            // if the ARPlaneManager is enabled, disable it
                            arPlaneManager.enabled = !arPlaneManager.enabled;
                            // Loop through all of the planes in the ARPlane Manager, and disable them.
                            foreach (ARPlane plane in arPlaneManager.trackables)
                            {
                                plane.gameObject.SetActive(arPlaneManager.enabled);
                            }

                            camStartPos = Camera.main.transform.position;

                        }
                    }
                }
            }
        }
    }



    public void DisplayFeet()
    {
        if (!areFeetDisplayed)
        {

            if (yPositionFound == true)
            {
                areFeetDisplayed = true;
                //get camera position of camera tagged as maincamera
                //var camPos = Camera.main.transform.position; // or whatever the floor position of the camera should be.

                //rotate camera based on function and camera position
                var camTransform = Camera.main.transform;
                viewRot = CalculateRotation(camTransform.forward);
                cameraXZPos = new Vector3(camTransform.position.x, 0, camTransform.position.z);



                //Instantiate Prefabs
                rightFootInGame = Instantiate(rightFootPrefab);
                leftFootInGame = Instantiate(leftFootPrefab);


                //Transform feet based on camera position
                //Adds on the camera's X and Z position, rotation and multiplies it by the foot separation. 
                localRightFootOffset = new Vector3(footSeparation / 2, yChosenPosition, zStartPosition);
                localLeftFootOffset = new Vector3(-footSeparation / 2, yChosenPosition, zStartPosition);

                //Rotates model to make it flat
                var LeftFootOrientation = viewRot * Quaternion.Euler(-90f, 180f, 0f);
                var RightFootOrientation = viewRot * Quaternion.Euler(-90f, 180f, 0f);

                rightFootInGame.transform.position += cameraXZPos + viewRot * localRightFootOffset;
                rightFootInGame.transform.rotation = RightFootOrientation;

                rightFootInGame.transform.localScale = new Vector3(-5f, 5f, 1f);

                leftFootInGame.transform.position += cameraXZPos + viewRot * localLeftFootOffset;
                leftFootInGame.transform.rotation = LeftFootOrientation;

                leftFootInGame.transform.localScale = new Vector3(5f, 5f, 1f);

                //Destroy(rightFootInGame, 10);
                //Destroy(leftFootInGame, 10);
            }
        }
    }

    public Quaternion CalculateRotation(Vector3 viewDir, float angleOffset = 0)
    {
        Vector3 newDirection = new Vector3(viewDir.x, 0, viewDir.z).normalized;
        var offsetRotation = Quaternion.Euler(0, angleOffset, 0);
        return Quaternion.FromToRotation(Vector3.forward, newDirection) * offsetRotation;
    }


    //Function to place feet in front of user when walking
    public void MoveFeet()
    {
        if (areFeetDisplayed)
        {

            if (Camera.main.transform.position.z > newPos)
            {
                //Left foot is instantiated first
                if (isLeft)
                {
                    isLeft = false;
                    newPos += 0.5f;
                    GameObject WalkingLeftFoot = Instantiate(leftFootPrefab);
                    WalkingLeftFoot.transform.position += cameraXZPos + viewRot * new Vector3(-footSeparation / 2, yChosenPosition, newPos);
                    WalkingLeftFoot.transform.rotation = CalculateRotation(Camera.main.transform.forward) * Quaternion.Euler(-90f, 180f, 0f);
                    WalkingLeftFoot.transform.localScale = new Vector3(5f, 5f, 5f);

                    Destroy(WalkingLeftFoot, 10);

                }

                else
                {
                    isLeft = true;
                    newPos += 0.5f;
                    GameObject WalkingRightFoot = Instantiate(rightFootPrefab);
                    WalkingRightFoot.transform.position += cameraXZPos + viewRot * new Vector3(footSeparation / 2, yChosenPosition, newPos);
                    WalkingRightFoot.transform.rotation = CalculateRotation(Camera.main.transform.forward) * Quaternion.Euler(-90f, 180f, 0f);
                    WalkingRightFoot.transform.localScale = new Vector3(-5f, 5f, 5f);

                    Destroy(WalkingRightFoot, 10);
                }
            }
        }
    }


    //Alternative code to get feet walking when the camera moves.

    //public Vector3 Vec3ToVec2(Vector3 v)
    //{
    //    return new Vector3(v.x, 0, v.z);
    //}

    //public void UpdateFeet()
    //{
    //    if (areFeetDisplayed)
    //    {
    //        Vector3 RightgroundPos = new Vector3(footSeparation / 2, yChosenPosition, 0);
    //        Vector3 LeftgroundPos = new Vector3(footSeparation, yChosenPosition, 0);

    //        var newCameraPos = Camera.main.transform.position;
    //        var camDifference = newCameraPos - camStartPos;

    //        Debug.Log("###########################");
    //        Debug.Log("Current CameraPosition" + newCameraPos);
    //        Debug.Log("Current StartPos" + camStartPos);
    //        Debug.Log("Cam Difference: This is the difference between the starting position and current position" + camDifference);


    //        var d = Vec3ToVec2(Camera.main.transform.forward);
    //        float distanceTraveled = Vector3.Dot(d, camDifference);

    //        Debug.Log("Dot Product / Distance Travelled" + distanceTraveled);

    //        Debug.Log("###########################");

    //        var stepDistance = 1f;

    //        leftFootInGame.transform.position = camStartPos + RightgroundPos + d * (Mathf.Round(distanceTraveled / stepDistance) * stepDistance);
    //        rightFootInGame.transform.position = camStartPos + LeftgroundPos + d * ((Mathf.Round(distanceTraveled + 0.5f) / stepDistance) * stepDistance);


    //    }
    //}

    //public Vector3 startPos;
    //public Vector3 cameraPos;
    //public Vector3 cameraFwd;

    //private void OnDrawGizmos()
    //{
    //    // the position which the camera is facing
    //    var d = cameraFwd.normalized;

    //    //Use the dot product to combine two vectors the camera forward position and start position
    //    float distanceTraveled = Vector3.Dot(d, cameraPos - startPos);

    //    Vector3 distanceMarker = startPos + d * distanceTraveled;
    //    Gizmos.DrawRay(startPos, d * 100);
    //    Gizmos.color = Color.black;
    //    Gizmos.DrawSphere(distanceMarker, 0.5f);

    //    // the distance of each step
    //    var stepDistance = 1f;
    //    Gizmos.color = Color.red;

    //    Gizmos.DrawSphere(startPos + d * (Mathf.Round(distanceTraveled / stepDistance) * stepDistance), 0.15f);
    //    Gizmos.color = Color.green;
    //    Gizmos.DrawSphere(startPos + d * (Mathf.Round((distanceTraveled + 0.5f) / stepDistance) * stepDistance), 0.15f);

    //}



}