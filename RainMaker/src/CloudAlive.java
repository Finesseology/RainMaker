//Cloud State for being onscreen
class CloudAlive extends CloudState {
    public CloudAlive(Cloud cloud) {
        super(cloud);
    }

    @Override
    void updateCloud() {
        cloud.setTranslateX(cloud.getTranslateX() + cloud.getSpeed() * .3);
    }
}
